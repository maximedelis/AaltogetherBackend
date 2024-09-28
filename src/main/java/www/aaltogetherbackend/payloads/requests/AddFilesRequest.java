package www.aaltogetherbackend.payloads.requests;

import java.util.Set;
import java.util.UUID;

public record AddFilesRequest(
        UUID roomId,
        Set<Long> fileIds
) {}
