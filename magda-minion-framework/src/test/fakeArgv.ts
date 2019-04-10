import { MinionArguments } from "../commonYargs";

export default function fakeArgv(options: {
    internalUrl: string;
    registryUrl: string;
    jwtSecret: string;
    userId: string;
    listenPort: number;
    tenantId: number;
}): MinionArguments {
    return {
        ...options,
        retries: 0
    };
}
