package com.bahubba.bahubbabookclub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import com.bahubba.bahubbabookclub.service.MembershipService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/** Unit tests for {@link MembershipController} endpoints */
@SpringBootTest
@ActiveProfiles("test")
class MembershipControllerTest {
    @Autowired
    MembershipController membershipController;

    @MockBean
    MembershipService membershipService;

    @Test
    void testGetAll() {
        when(membershipService.getAll(anyString(), anyInt(), anyInt())).thenReturn(Page.empty());

        ResponseEntity<Page<BookClubMembershipDTO>> rsp = membershipController.getAll("foo", 1, 1);

        verify(membershipService, times(1)).getAll(anyString(), anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetRole() {
        when(membershipService.getRole(anyString())).thenReturn(BookClubRole.USER);

        ResponseEntity<BookClubRole> rsp = membershipController.getRole("foo");

        verify(membershipService, times(1)).getRole(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetMembership() {
        when(membershipService.getMembership(anyString()))
                .thenReturn(BookClubMembershipDTO.builder().build());

        ResponseEntity<BookClubMembershipDTO> rsp = membershipController.getMembership("foo");

        verify(membershipService, times(1)).getMembership(anyString());
        assertThat(rsp).isNotNull();
    }

    @Test
    void testUpdateMembership() {
        when(membershipService.updateMembership(any()))
                .thenReturn(BookClubMembershipDTO.builder().build());

        ResponseEntity<BookClubMembershipDTO> rsp =
                membershipController.updateMembership(MembershipUpdate.builder().build());

        verify(membershipService, times(1)).updateMembership(any());
        assertThat(rsp).isNotNull();
    }

    @Test
    void testDeleteMembership() {
        when(membershipService.deleteMembership(anyString(), any(UUID.class)))
                .thenReturn(BookClubMembershipDTO.builder().build());

        ResponseEntity<BookClubMembershipDTO> rsp = membershipController.deleteMembership("foo", UUID.randomUUID());

        verify(membershipService, times(1)).deleteMembership(anyString(), any(UUID.class));
        assertThat(rsp).isNotNull();
    }

    @Test
    void testChangeOwnership() {
        when(membershipService.addOwner(any(NewOwner.class))).thenReturn(true);

        ResponseEntity<Boolean> rsp =
                membershipController.addOwner(NewOwner.builder().build());

        verify(membershipService, times(1)).addOwner(any(NewOwner.class));
        assertThat(rsp).isNotNull();
    }

    @Test
    void testRevokeOwnership() {
        when(membershipService.revokeOwnership(any(MembershipCompositeID.class)))
                .thenReturn(BookClubMembershipDTO.builder().build());

        ResponseEntity<BookClubMembershipDTO> rsp = membershipController.revokeOwnership(
                MembershipCompositeID.builder().build());

        verify(membershipService, times(1)).revokeOwnership(any(MembershipCompositeID.class));
        assertThat(rsp).isNotNull();
    }
}
