package fpt.edu.sep490.pilahub.pojo;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostReactionId implements Serializable {
    private UUID account;
    private UUID post;
}

