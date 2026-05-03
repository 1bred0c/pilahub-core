package pilahub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentManagementResponse {
    private boolean success;
    private String message;
    private String fileName;
    private String fileUri;
    private String state;
    private String displayName;
    private Long sizeBytes;
    private String expirationTime;
}

