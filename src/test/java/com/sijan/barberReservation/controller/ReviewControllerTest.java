package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.review.CreateReplyRequest;
import com.sijan.barberReservation.DTO.review.CreateReviewRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.review.ReviewMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Review;
import com.sijan.barberReservation.model.ReviewType;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ReviewMapper reviewMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private PageMapper pageMapper;

    private Customer testCustomer;
    private Review testReview;
    private ReviewDTO testReviewDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setRating(5);
        testReview.setComment("Great service!");

        testReviewDTO = new ReviewDTO();
        testReviewDTO.setId(1L);
        testReviewDTO.setRating(5);
        testReviewDTO.setComment("Great service!");
    }

    @Test
    @WithMockUser
    void createReview_Success() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("Great service!");
        request.setTargetId(1L);
        request.setTargetType(ReviewType.BARBER_SHOP);

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(reviewMapper.toEntity(any(), any())).thenReturn(testReview);
        when(reviewService.createReview(any())).thenReturn(testReviewDTO);

        mockMvc.perform(post("/api/reviews/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService).createReview(any());
    }

    @Test
    @WithMockUser
    void getReviews_Success() throws Exception {
        Page<Review> page = new PageImpl<>(Arrays.asList(testReview));
        PageResponse<ReviewDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(Arrays.asList(testReviewDTO));

        when(reviewService.getReviews(any(), anyLong(), any())).thenReturn(page);
        when(pageMapper.toReviewPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/reviews")
                        .param("type", "BARBER_SHOP")
                        .param("targetId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(reviewService).getReviews(any(), anyLong(), any());
    }

    @Test
    @WithMockUser
    void replyToReview_Success() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest();
        request.setComment("Thank you for your feedback!");

        when(reviewService.replyToReview(anyLong(), anyLong(), any())).thenReturn(testReview);
        when(reviewMapper.toDTO(any())).thenReturn(testReviewDTO);

        mockMvc.perform(post("/api/reviews/1/reply/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(reviewService).replyToReview(anyLong(), anyLong(), any());
    }
}
