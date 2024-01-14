package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/** Unit tests for the {@link MembershipService} business logic */
@SpringBootTest
@ActiveProfiles("test")
class MembershipServiceTest {
    @Autowired
    private MembershipService membershipService;

    @MockBean
    BookClubRepo bookClubRepo;

    @MockBean
    BookClubMembershipRepo bookClubMembershipRepo;

    @Test
    void testGetAll() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndUserId(
                        anyString(), any(BookClubRole.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .clubRole(BookClubRole.ADMIN)
                        .build()));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BookClubMembershipDTO> result = membershipService.getAll("foo", 1, 1);

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndClubRoleAndUserId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_UserNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> membershipService.getAll("foo", 1, 1));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndClubRoleAndUserId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_UserNotMemberOrNotAdmin() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndUserId(
                        anyString(), any(BookClubRole.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedBookClubActionException.class, () -> membershipService.getAll("foo", 1, 1));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndClubRoleAndUserId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_NegativePageSize() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndUserId(
                        anyString(), any(BookClubRole.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .clubRole(BookClubRole.ADMIN)
                        .build()));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> membershipService.getAll("foo", 1, -1));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndClubRoleAndUserId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetAll_TooLargePageSize() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndClubRoleAndUserId(
                        anyString(), any(BookClubRole.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .clubRole(BookClubRole.ADMIN)
                        .build()));
        when(bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> membershipService.getAll("foo", 1, 51));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndClubRoleAndUserId(anyString(), any(BookClubRole.class), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).findAllByBookClubNameOrderByJoined(anyString(), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetRole() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubNameAndUserId(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(new BookClubMembership()));

        BookClubRole result = membershipService.getRole("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testGetRole_NoUser() {
        assertThrows(UserNotFoundException.class, () -> membershipService.getRole("foo"));
    }

    @Test
    void testGetMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserId(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(new BookClubMembership()));
        BookClubMembershipDTO result = membershipService.getMembership("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetMembership_NoUser() {
        assertThrows(UserNotFoundException.class, () -> membershipService.getMembership("foo"));
    }

    @Test
    void testGetMembership_NoMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserId(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());
        when(bookClubRepo.findByName(anyString()))
                .thenReturn(Optional.of(BookClub.builder().build()));
        BookClubMembershipDTO result = membershipService.getMembership("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership() {
        UUID testUserID = UUID.randomUUID();
        UUID testUpdateUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .user(User.builder().id(testUpdateUserID).build())
                        .isOwner(false)
                        .build()));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        BookClubMembershipDTO result = membershipService.updateMembership(MembershipUpdate.builder()
                .bookClubName("foo")
                .userID(testUpdateUserID)
                .role(BookClubRole.ADMIN)
                .build());

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> membershipService.updateMembership(
                        MembershipUpdate.builder().build()));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_UpdatingSelf() {
        UUID testUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        assertThrows(
                BadBookClubActionException.class,
                () -> membershipService.updateMembership(MembershipUpdate.builder()
                        .bookClubName("foo")
                        .userID(testUserID)
                        .role(BookClubRole.ADMIN)
                        .build()));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_RequesterNotMember() {
        UUID testUserID = UUID.randomUUID();
        UUID testUpdateUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedBookClubActionException.class,
                () -> membershipService.updateMembership(MembershipUpdate.builder()
                        .bookClubName("foo")
                        .userID(testUpdateUserID)
                        .role(BookClubRole.ADMIN)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_TargetUserNotMember() {
        UUID testUserID = UUID.randomUUID();
        UUID testUpdateUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                MembershipNotFoundException.class,
                () -> membershipService.updateMembership(MembershipUpdate.builder()
                        .bookClubName("foo")
                        .userID(testUpdateUserID)
                        .role(BookClubRole.ADMIN)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_UpdateOwner() {
        UUID testUserID = UUID.randomUUID();
        UUID testUpdateUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .user(User.builder().id(testUpdateUserID).build())
                        .isOwner(true)
                        .build()));

        assertThrows(
                BadBookClubActionException.class,
                () -> membershipService.updateMembership(MembershipUpdate.builder()
                        .bookClubName("foo")
                        .userID(testUpdateUserID)
                        .role(BookClubRole.ADMIN)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdateMembership_NoUpdate() {
        UUID testUserID = UUID.randomUUID();
        UUID testUpdateUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .user(User.builder().id(testUpdateUserID).build())
                        .clubRole(BookClubRole.ADMIN)
                        .isOwner(false)
                        .build()));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        assertThrows(
                BadBookClubActionException.class,
                () -> membershipService.updateMembership(MembershipUpdate.builder()
                        .bookClubName("foo")
                        .userID(testUpdateUserID)
                        .role(BookClubRole.ADMIN)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership() {
        UUID testUserID = UUID.randomUUID();
        UUID testDeleteUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .user(User.builder().id(testDeleteUserID).build())
                        .isOwner(false)
                        .build()));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        BookClubMembershipDTO result = membershipService.deleteMembership("foo", testDeleteUserID);

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> membershipService.deleteMembership("foo", UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_DeletingSelf() {
        UUID testUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        assertThrows(BadBookClubActionException.class, () -> membershipService.deleteMembership("foo", testUserID));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_RequesterNotAdmin() {
        UUID testUserID = UUID.randomUUID();
        UUID testDeleteUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedBookClubActionException.class,
                () -> membershipService.deleteMembership("foo", testDeleteUserID));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_targetUserNotMember() {
        UUID testUserID = UUID.randomUUID();
        UUID testDeleteUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                MembershipNotFoundException.class, () -> membershipService.deleteMembership("foo", testDeleteUserID));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDeleteMembership_targetUserOwner() {
        UUID testUserID = UUID.randomUUID();
        UUID testDeleteUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .user(User.builder().id(testDeleteUserID).build())
                        .isOwner(true)
                        .build()));

        assertThrows(
                BadBookClubActionException.class, () -> membershipService.deleteMembership("foo", testDeleteUserID));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        anyString(), any(UUID.class), any(BookClubRole.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership() {
        UUID testUserID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .bookClub(BookClub.builder().build())
                        .user(User.builder().id(testNewOwnerID).build())
                        .isOwner(false)
                        .build()));
        when(bookClubMembershipRepo.save(any(BookClubMembership.class))).thenReturn(new BookClubMembership());

        Boolean result = membershipService.addOwner(NewOwner.builder()
                .bookClubName("foo")
                .newOwnerID(testNewOwnerID)
                .build());

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        assertThat(result).isTrue();

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> membershipService.addOwner(NewOwner.builder()
                        .bookClubName("foo")
                        .newOwnerID(UUID.randomUUID())
                        .build()));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NoOwnerChange() {
        UUID testUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        assertThrows(
                BadBookClubActionException.class,
                () -> membershipService.addOwner(NewOwner.builder()
                        .bookClubName("foo")
                        .newOwnerID(testUserID)
                        .build()));

        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NotOwner() {
        UUID testUserID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedBookClubActionException.class,
                () -> membershipService.addOwner(NewOwner.builder()
                        .bookClubName("foo")
                        .newOwnerID(testNewOwnerID)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testChangeOwnership_NewOwnerNotMember() {
        UUID testUserID = UUID.randomUUID();
        UUID testNewOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder().build()));
        when(bookClubMembershipRepo.findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                MembershipNotFoundException.class,
                () -> membershipService.addOwner(NewOwner.builder()
                        .bookClubName("foo")
                        .newOwnerID(testNewOwnerID)
                        .build()));

        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndIsOwnerTrue(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(1))
                .findByBookClubNameAndUserIdAndDepartedIsNull(anyString(), any(UUID.class));
        verify(bookClubMembershipRepo, times(0)).save(any(BookClubMembership.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testRevokeOwnership() {
        UUID testUserID = UUID.randomUUID();
        UUID targetOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(
                        any(UUID.class), anyList()))
                .thenReturn(List.of(
                        BookClubMembership.builder()
                                .user(User.builder().id(testUserID).build())
                                .build(),
                        BookClubMembership.builder()
                                .user(User.builder().id(targetOwnerID).build())
                                .isOwner(true)
                                .build()));

        when(bookClubMembershipRepo.save(any(BookClubMembership.class)))
                .thenReturn(BookClubMembership.builder().build());

        BookClubMembershipDTO result = membershipService.revokeOwnership(MembershipCompositeID.builder()
                .bookClubID(UUID.randomUUID())
                .userID(targetOwnerID)
                .build());

        verify(bookClubMembershipRepo, times(1))
                .findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(any(UUID.class), anyList());

        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testRevokeOwnership_UserNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> membershipService.revokeOwnership(
                        MembershipCompositeID.builder().build()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testRevokeOwnership_RevokingOwnOwnership() {
        UUID testUserID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        assertThrows(
                BadBookClubActionException.class,
                () -> membershipService.revokeOwnership(
                        MembershipCompositeID.builder().userID(testUserID).build()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testRevokeOwnership_UserNotOwner() {
        UUID testUserID = UUID.randomUUID();
        UUID targetOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(
                        any(UUID.class), anyList()))
                .thenReturn(List.of(BookClubMembership.builder()
                        .user(User.builder().id(targetOwnerID).build())
                        .isOwner(false)
                        .build()));

        assertThrows(
                UnauthorizedBookClubActionException.class,
                () -> membershipService.revokeOwnership(MembershipCompositeID.builder()
                        .bookClubID(UUID.randomUUID())
                        .userID(targetOwnerID)
                        .build()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testRevokeOwnership_TargetUserNotOwner() {
        UUID testUserID = UUID.randomUUID();
        UUID targetOwnerID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testUserID).build());

        when(bookClubMembershipRepo.findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(
                        any(UUID.class), anyList()))
                .thenReturn(List.of(BookClubMembership.builder()
                        .user(User.builder().id(testUserID).build())
                        .build()));

        assertThrows(
                MembershipNotFoundException.class,
                () -> membershipService.revokeOwnership(MembershipCompositeID.builder()
                        .bookClubID(UUID.randomUUID())
                        .userID(targetOwnerID)
                        .build()));

        securityUtilMockedStatic.close();
    }
}
