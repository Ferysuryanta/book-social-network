package com.network.feedback;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackResponse {

    private Double note;
    private String comments;
    private boolean ownFeedback;
}
