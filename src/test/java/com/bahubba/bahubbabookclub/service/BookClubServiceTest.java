package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.S3ImageDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import com.bahubba.bahubbabookclub.model.payload.BookClubPayload;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.model.S3Object;

/** Unit tests for the {@link BookClubService} business logic */
@SpringBootTest
@ActiveProfiles("test")
class BookClubServiceTest {
    @Autowired
    BookClubService bookClubService;

    @MockBean
    S3Service s3Service;

    @MockBean
    BookClubRepo bookClubRepo;

    @MockBean
    BookClubMembershipRepo bookClubMembershipRepo;

    @MockBean
    NotificationRepo notificationRepo;

    @BeforeEach
    void setUp() {
        when(s3Service.getPreSignedURL(anyString())).thenReturn("https://test.com");
    }

    @Test
    void testCreate() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(new User());

        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result =
                bookClubService.create(BookClubPayload.builder().name("Test").build());
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        verify(bookClubMembershipRepo, times(1)).save(any(BookClubMembership.class));
        verify(notificationRepo, times(1)).save(any(Notification.class));
        assertThat(result).isNotNull();

        securityUtilMockedStatic.close();
    }

    @Test
    void testCreate_ReservedName() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(new User());

        assertThrows(
                BadBookClubActionException.class,
                () -> bookClubService.create(
                        BookClubPayload.builder().name("Default").build()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testCreate_NoUser() {
        assertThrows(
                UserNotFoundException.class,
                () -> bookClubService.create(BookClubPayload.builder().build()));
    }

    @Test
    void testUpdate() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findByIdAndUserIsAdmin(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(new BookClub()));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.update(BookClubPayload.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .imageFileName("foobar")
                .build());

        verify(bookClubRepo, times(1)).findByIdAndUserIsAdmin(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testUpdate_NoUser() {
        assertThrows(
                UserNotFoundException.class,
                () -> bookClubService.update(BookClubPayload.builder().build()));

        verify(bookClubRepo, times(0)).findByIdAndUserIsAdmin(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));
    }

    @Test
    void testUpdate_NotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findByIdAndUserIsAdmin(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                BookClubNotFoundException.class,
                () -> bookClubService.update(
                        BookClubPayload.builder().id(UUID.randomUUID()).build()));

        verify(bookClubRepo, times(1)).findByIdAndUserIsAdmin(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByID() {
        // imageUploaded set here to add coverage for BookClubAspect
        when(bookClubRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(
                        BookClub.builder().publicity(Publicity.PUBLIC).build()));
        BookClubDTO result = bookClubService.findByID(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID_Private() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(BookClub.builder()
                        .id(UUID.randomUUID())
                        .publicity(Publicity.PRIVATE)
                        .build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(true);
        BookClubDTO result = bookClubService.findByID(UUID.randomUUID());
        verify(bookClubRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByID_NotFound() {
        when(bookClubRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));
    }

    @Test
    void testFindByName() {
        when(bookClubRepo.findByName(anyString()))
                .thenReturn(Optional.of(
                        BookClub.builder().publicity(Publicity.PUBLIC).build()));
        BookClubDTO result = bookClubService.findByName("foo");
        verify(bookClubRepo, times(1)).findByName(anyString());
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByName_Private() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findByName(anyString()))
                .thenReturn(Optional.of(BookClub.builder()
                        .id(UUID.randomUUID())
                        .publicity(Publicity.PRIVATE)
                        .build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(true);
        BookClubDTO result = bookClubService.findByName("foo");
        verify(bookClubRepo, times(1)).findByName(anyString());
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindByName_NotFound() {
        when(bookClubRepo.findByName(anyString())).thenReturn(Optional.empty());
        assertThrows(BookClubNotFoundException.class, () -> bookClubService.findByName("foo"));
    }

    @Test
    void testFindAllForUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        // Book clubs filled in here to add coverage for BookClubAspect
        when(bookClubRepo.findAllForUser(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        BookClub.builder().build(),
                        BookClub.builder().name("Test").build())));
        Page<BookClubDTO> result = bookClubService.findAllForUser(1, 1);
        verify(bookClubRepo, times(1)).findAllForUser(any(UUID.class), any(Pageable.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAllForUser_NoUser() {
        assertThrows(UserNotFoundException.class, () -> bookClubService.findAllForUser(1, 1));
    }

    @Test
    void testFindAllForUser_NegativePageSize() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findAllForUser(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.findAllForUser(1, -1));
        verify(bookClubRepo, times(1)).findAllForUser(any(UUID.class), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAllForUser_TooLargePageSize() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findAllForUser(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.findAllForUser(1, 51));
        verify(bookClubRepo, times(1)).findAllForUser(any(UUID.class), any(Pageable.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testFindAll() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        Page<BookClubDTO> result = bookClubService.findAll(1, 1);
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindAll_NegativePageSize() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.findAll(1, -1));
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testFindAll_TooLargePageSize() {
        when(bookClubRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.findAll(1, 51));
        verify(bookClubRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testDisbandBookClubByID() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .isOwner(true)
                        .build()));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.disbandBookClubByID(UUID.randomUUID());

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndUserId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByID_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));
        verify(bookClubMembershipRepo, times(0)).findByBookClubIdAndUserId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByID_MembershipNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndUserId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClub_UserNotOwner() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());

        when(bookClubMembershipRepo.findByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .isOwner(false)
                        .build()));

        assertThrows(
                UnauthorizedBookClubActionException.class,
                () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));
        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndUserId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClub_AlreadyDisbanded() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder()
                                .id(UUID.randomUUID())
                                .disbanded(LocalDateTime.now())
                                .build())
                        .isOwner(true)
                        .build()));

        assertThrows(BadBookClubActionException.class, () -> bookClubService.disbandBookClubByID(UUID.randomUUID()));

        verify(bookClubMembershipRepo, times(1)).findByBookClubIdAndUserId(any(UUID.class), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName() {
        UUID testID = UUID.randomUUID();

        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(testID).build());
        when(bookClubMembershipRepo.findByBookClubNameAndUserId(anyString(), any(UUID.class)))
                .thenReturn(Optional.of(BookClubMembership.builder()
                        .user(User.builder().id(testID).build())
                        .bookClub(BookClub.builder().id(UUID.randomUUID()).build())
                        .isOwner(true)
                        .build()));
        when(bookClubRepo.save(any(BookClub.class))).thenReturn(new BookClub());

        BookClubDTO result = bookClubService.disbandBookClubByName("foo");

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(1)).save(any(BookClub.class));
        assertThat(result).isNotNull();
        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> bookClubService.disbandBookClubByName("foo"));
        verify(bookClubMembershipRepo, times(0)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testDisbandBookClubByName_MembershipNotFound() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubMembershipRepo.findByBookClubNameAndUserId(anyString(), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class, () -> bookClubService.disbandBookClubByName("foo"));

        verify(bookClubMembershipRepo, times(1)).findByBookClubNameAndUserId(anyString(), any(UUID.class));
        verify(bookClubRepo, times(0)).save(any(BookClub.class));

        securityUtilMockedStatic.close();
    }

    @Test
    void testSearch() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
                        any(Publicity.class), anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BookClubDTO> result = bookClubService.search("foo", 1, 1);

        verify(bookClubRepo, times(1))
                .findAllByPublicityNotAndNameContainsIgnoreCase(any(Publicity.class), anyString(), any(Pageable.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testSearch_NegativePageSize() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
                        any(Publicity.class), anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(PageSizeTooSmallException.class, () -> bookClubService.search("foo", 1, -1));
        verify(bookClubRepo, times(1))
                .findAllByPublicityNotAndNameContainsIgnoreCase(any(Publicity.class), anyString(), any(Pageable.class));
    }

    @Test
    void testSearch_TooLargePageSize() {
        when(bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
                        any(Publicity.class), anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(PageSizeTooLargeException.class, () -> bookClubService.search("foo", 1, 51));
        verify(bookClubRepo, times(1))
                .findAllByPublicityNotAndNameContainsIgnoreCase(any(Publicity.class), anyString(), any(Pageable.class));
    }

    @Test
    void testCheckBookClubMembership_NoUser() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUserDetails).thenReturn(null);
        when(bookClubRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(
                        BookClub.builder().publicity(Publicity.PRIVATE).build()));

        assertThrows(UserNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testCheckBookClubMembership_NoMembership() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        securityUtilMockedStatic
                .when(SecurityUtil::getCurrentUserDetails)
                .thenReturn(User.builder().id(UUID.randomUUID()).build());
        when(bookClubRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(
                        BookClub.builder().publicity(Publicity.PRIVATE).build()));
        when(bookClubMembershipRepo.existsByBookClubIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(false);

        assertThrows(MembershipNotFoundException.class, () -> bookClubService.findByID(UUID.randomUUID()));

        securityUtilMockedStatic.close();
    }

    @Test
    void testGetPreSignedStockBookClubImageURLs() {
        when(s3Service.listS3ObjectsAtPrefix(anyString()))
                .thenReturn(List.of(
                        S3Object.builder().key("test").size(1L).build(),
                        S3Object.builder().key("test2").size(0L).build()));

        List<S3ImageDTO> result = bookClubService.getStockBookClubImages();

        verify(s3Service, times(1)).listS3ObjectsAtPrefix(anyString());
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }
}
