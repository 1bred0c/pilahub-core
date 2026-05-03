package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia {
    @Id
    @GeneratedValue
    @Column(name = "post_media_id", nullable = false, updatable = false)
    private UUID postMediaId;

    @NotNull(message = "Post must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @NotNull(message = "Media type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @NotNull(message = "Media URL must not be null")
    @Size(max = 500, message = "Media URL must not exceed 500 characters")
    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @NotNull(message = "Sort order must not be null")
    @Min(value = 1, message = "Sort order must be at least 1")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
