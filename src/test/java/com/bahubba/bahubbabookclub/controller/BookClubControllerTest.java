package com.bahubba.bahubbabookclub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.S3ImageDTO;
import com.bahubba.bahubbabookclub.model.payload.BookClubPayload;
import com.bahubba.bahubbabookclub.model.payload.BookClubSearch;
import com.bahubba.bahubbabookclub.service.BookClubService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/** Unit tests for {@link BookClubController} endpoints */
@SpringBootTest
@ActiveProfiles("test")
class BookClubControllerTest {
    @Autowired
    BookClubController bookClubController;

    @MockBean
    BookClubService bookClubService;

    @Test
    void testCreate() {
        when(bookClubService.create(any(BookClubPayload.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp =
                bookClubController.create(BookClubPayload.builder().build());
        verify(bookClubService, times(1)).create(any(BookClubPayload.class));
        assertThat(rsp).isNotNull();
    }

    @Test
    void testUpdate() {
        when(bookClubService.update(any(BookClubPayload.class))).thenReturn(new BookClubDTO());

        ResponseEntity<BookClubDTO> rsp = bookClubController.update(new BookClubPayload());

        verify(bookClubService, times(1)).update(any(BookClubPayload.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetByID() {
        when(bookClubService.findByID(any(UUID.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByID(UUID.randomUUID());
        verify(bookClubService, times(1)).findByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetByName() {
        when(bookClubService.findByName(anyString())).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.getByName("foo");
        verify(bookClubService, times(1)).findByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAllForUser() {
        when(bookClubService.findAllForUser(anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.getAllForUser(1, 1);
        verify(bookClubService, times(1)).findAllForUser(anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAll() {
        when(bookClubService.findAll(anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.getAll(1, 1);
        verify(bookClubService, times(1)).findAll(anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testDisbandBookClub() {
        when(bookClubService.disbandBookClubByID(any(UUID.class))).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.disbandBookClub(UUID.randomUUID());
        verify(bookClubService, times(1)).disbandBookClubByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testDisbandBookClubByName() {
        when(bookClubService.disbandBookClubByName(anyString())).thenReturn(new BookClubDTO());
        ResponseEntity<BookClubDTO> rsp = bookClubController.disbandBookClubByName("foo");
        verify(bookClubService, times(1)).disbandBookClubByName(anyString());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testSearch() {
        when(bookClubService.search(anyString(), anyInt(), anyInt())).thenReturn(Page.empty());
        ResponseEntity<Page<BookClubDTO>> rsp = bookClubController.search(BookClubSearch.builder()
                .searchTerm("foo")
                .pageNum(1)
                .pageSize(1)
                .build());
        verify(bookClubService, times(1)).search(anyString(), anyInt(), anyInt());
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetPreSignedStockBookClubImageURLs() {
        when(bookClubService.getStockBookClubImages()).thenReturn(new ArrayList<>());

        ResponseEntity<List<S3ImageDTO>> rsp = bookClubController.getPreSignedStockBookClubImageURLs();

        verify(bookClubService, times(1)).getStockBookClubImages();
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
