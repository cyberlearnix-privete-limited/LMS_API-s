package com.lms.wishlist_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// REMOVED: @JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private T data; // Now this will show up as "data": null instead of disappearing
    private String message;
}