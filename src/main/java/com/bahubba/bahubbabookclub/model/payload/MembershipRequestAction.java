package com.bahubba.bahubbabookclub.model.payload;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.RequestAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data sent with HTTP request for taking an action on a membership request */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipRequestAction {
    private MembershipRequestDTO membershipRequest;
    private RequestAction action;
    private BookClubRole role;
    private String reviewMessage;
}
