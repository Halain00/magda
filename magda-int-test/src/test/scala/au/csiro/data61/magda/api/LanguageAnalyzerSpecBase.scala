package au.csiro.data61.magda.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Route
import au.csiro.data61.magda.api.model.SearchResult
import au.csiro.data61.magda.model.misc._
import au.csiro.data61.magda.search.SearchStrategy
import au.csiro.data61.magda.test.util.{Generators, MagdaMatchers}
import org.scalacheck._

import scala.collection.immutable
import scala.concurrent.{Await, Future}

trait LanguageAnalyzerSpecBase extends BaseSearchApiSpec {

  def isAStopWord(term: String): Boolean = Generators.luceneStopWords.exists(stopWord => term.trim.equalsIgnoreCase(stopWord))

  def getAllSlices(terms: Seq[String]): immutable.IndexedSeq[Seq[String]] = for {
    start <- terms.indices
    len <- 1 to (terms.length - start)
    combinations = terms.slice(start, start + len)
  } yield combinations

  def testLanguageFieldSearch(outerTermExtractor: DataSet => Seq[String],
                              test: (DataSet, String, Route, List[(DataSet, String)]) => Unit, keepOrder: Boolean = false,
                              useLightEnglishStemmer: Boolean = false,
                              testWhat: String): Unit = {
    it("when searching for it directly") {
      def innerTermExtractor(dataSet: DataSet) = if (keepOrder) {
        outerTermExtractor(dataSet).map(term => MagdaMatchers.tokenize(term).map(_.trim)).flatMap(getAllSlices).map(_.mkString(" "))
      } else {
        outerTermExtractor(dataSet).flatMap(MagdaMatchers.tokenize)
      }

      doTest(innerTermExtractor, keepOrder, s"$testWhat, when searching for it directly")
    }

    it(s"regardless of pluralization/depluralization") {

      def innerTermExtractor(dataSet: DataSet) =
        if (keepOrder) {
          // If we're keeping order we want to create terms that are sub-slices of the terms created by outerTermExtractor
          outerTermExtractor(dataSet)
            // Split everything into individual words but don't mix the words from terms together
            .map(term =>
            MagdaMatchers.tokenize(term).map(_.trim))
            // Generate all subslices from those terms
            .flatMap(getAllSlices)
            // Get rid of all the subslices that feature words we can't pluralize or unpluralize reliably
            .filterNot(terms =>
            terms.exists(term =>
              term.contains(".") ||
                term.contains("'") ||
                term.equalsIgnoreCase("s") ||
                term.toLowerCase.endsWith("ss") ||
                term.toLowerCase.endsWith("e") ||
                term.toLowerCase.endsWith("ies") ||
                term.toLowerCase.endsWith("es") ||
                term.toLowerCase.endsWith("y") ||
                isAStopWord(term)))
            // Pluralize/depluralize individual words in each subslice where possible - if we can't
            // do it reliably then discard the entire subslice
            .map { terms =>
            val pluralized = terms.map {
              case term if term.last.toLower.equals('s') =>
                val depluralized = term.take(term.length - 1)
                if (MagdaMatchers.stemString(term, useLightEnglishStemmer) == depluralized) {
                  Some(depluralized)
                } else None
              case term =>
                val pluralized = term + "s"
                if (MagdaMatchers.stemString(pluralized, useLightEnglishStemmer) == term) {
                  Some(pluralized)
                } else None
            }

            if (pluralized.forall(_.isDefined)) pluralized.map(_.get) else Seq()
          }
            // Check we haven't introduced more stop words by pluralizing.
            .filterNot(terms => terms.exists(term => isAStopWord(term)))
            .map(_.mkString(" "))
        } else {
          // If we don't care about order then we just split all the terms into their individual words and
          // filter out ones that won't work
          outerTermExtractor(dataSet)
            .flatMap(MagdaMatchers.tokenize)
            .view
            .map(_.trim)
            .filterNot(_.contains("."))
            .filterNot(_.contains("'"))
            .filterNot(_.toLowerCase.endsWith("ss"))
            .filterNot(x => x.equalsIgnoreCase("and") || x.equalsIgnoreCase("or"))
            .filterNot(_.isEmpty)
            .filterNot(term => term.toLowerCase.endsWith("e") ||
              term.toLowerCase.endsWith("ies") ||
              term.toLowerCase.endsWith("es") ||
              term.toLowerCase.endsWith("y")) // This plays havoc with pluralization because when you add "s" to it, ES chops off the "es at the end
            .filterNot(isAStopWord)
            .flatMap {
              case term if term.last.toLower.equals('s') =>
                val depluralized = term.take(term.length - 1)
                if (MagdaMatchers.stemString(term, useLightEnglishStemmer) == depluralized) {
                  Some(depluralized)
                } else None
              case term =>
                val pluralized = term + "s"
                if (MagdaMatchers.stemString(pluralized, useLightEnglishStemmer) == term) {
                  Some(pluralized)
                } else None
            }
            .filterNot(isAStopWord)
        }

      doTest(innerTermExtractor, keepOrder, s"$testWhat, regardless of pluralization/depluralization")
    }

    def doTest(innerTermExtractor: DataSet => Seq[String], keepOrder: Boolean, testWhat: String): Unit = {
      def getIndividualTerms(terms: Seq[String]) = terms.map(MagdaMatchers.tokenize)

      /** Checks that there's at least one searchable term in this seq of strings */
      def checkForSearchableTerm = (list: Seq[String]) =>
        list.forall(_.length > 2) &&
          list.forall(_ matches ".*[A-Za-z].*") &&
          list.exists(term => !Seq("and", "or").contains(term.trim.toLowerCase)) &&
          list.exists(!isAStopWord(_))

      val indexAndTermsGen: Gen[(Gen[List[(DataSet, String)]], Future[(String, List[DataSet], Route)])] = for {
        gen <- smallIndexGen
        dataSetsRaw = gen._2
        indexedDataSets = dataSetsRaw.filterNot(dataSet ⇒ innerTermExtractor(dataSet).isEmpty)
        dataSetAndTermGens = indexedDataSets.flatMap { dataSet =>
          val rawTerms = getIndividualTerms(innerTermExtractor(dataSet))

          val termGen = if (keepOrder) {
            val validTerms = rawTerms.filter(checkForSearchableTerm)

            // Make sure there's _some_ sublist that can be successfully searched - if so try to generate one, otherwise return none
            if (validTerms.nonEmpty) {
              Some(Gen.oneOf(validTerms))
            } else None
          } else {
            val terms = rawTerms.flatten
              .filter(_.length > 2)
              .filterNot(term => Seq("and", "or", "").contains(term.trim.toLowerCase))
              .filterNot(isAStopWord)

            if (terms.nonEmpty) {
              Some(for {
                noOfTerms <- Gen.choose(1, terms.length)
                selectedTerms <- Gen.pick(noOfTerms, terms) //Gen.pick shuffles the order
              } yield selectedTerms)
            } else None
          }

          termGen.map(gen => gen.map(list => (dataSet, list.mkString(" ")))).toSeq
        }

        combinedDataSetAndTermGen = dataSetAndTermGens.foldRight(Gen.const(List[(DataSet, String)]()))((soFar, current) => {
          for {
            currentInner <- current
            list <- soFar
          } yield currentInner :+ list
        }).map(x => x).filter(x => x.nonEmpty)

      }yield (combinedDataSetAndTermGen, gen._1)

      forAll(indexAndTermsGen) {
        case (dataSetAndTerms, future) => {
          val resultF = future.map( gen => {
            assert(gen._2.nonEmpty)
            forAll(dataSetAndTerms) {
              list => {
                gen._2.foreach(dataSet => test(dataSet, gen._1, gen._3, list))
              }
            }
          })
          Await.result(resultF, SINGLE_TEST_WAIT_TIME)
        }
      }
    }
  }

  def testDataSetSearch(rawTermExtractor: DataSet => Seq[String], useLightEnglishStemmer: Boolean = false, testWhat: String): Unit = {
    def outerTermExtractor(dataSet: DataSet) = rawTermExtractor(dataSet)
      .filter(term => term.matches(".*[A-Za-z].*"))
      .filterNot(term => Generators.luceneStopWords.exists(stopWord => term.equals(stopWord.toLowerCase)))

    def test(dataSet: DataSet, term: String, routes: Route, tuples: List[(DataSet, String)]) = {
      Get(s"""/v0/datasets?query=${encodeForUrl(term)}&limit=10000""") ~> addSingleTenantIdHeader ~> routes ~> check {
        status shouldBe OK
        val result = responseAs[SearchResult]

        withClue(s"term: $term for ${outerTermExtractor(dataSet)} in ${result.dataSets.map(dataSet => dataSet.identifier + ": " + outerTermExtractor(dataSet)).mkString(", ")}") {
          result.strategy.get should equal(SearchStrategy.MatchAll)
          result.dataSets.size should be > 0
          result.dataSets.exists(_.identifier.equals(dataSet.identifier)) shouldBe true
        }
      }

      Get(s"""/v0/datasets?query=${encodeForUrl(term)}&limit=10000""") ~> addTenantIdHeader(tenant1) ~> routes ~> check {
        status shouldBe OK
        val result = responseAs[SearchResult]
        result.hitCount shouldBe 0
      }
    }

    testLanguageFieldSearch(outerTermExtractor, test, keepOrder = false, useLightEnglishStemmer = useLightEnglishStemmer, testWhat)
  }

}
