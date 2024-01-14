package com.bahubba.bahubbabookclub.model.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The composite key of a book club membership, passed when taking actions on that membership */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipCompositeID {
    private UUID bookClubID;
    private UUID userID;
}
