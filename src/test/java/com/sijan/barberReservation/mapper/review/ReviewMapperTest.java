package com.sijan.barberReservation.mapper.review;

import com.sijan.barberReservation.DTO.review.CreateReviewRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapper();
    }

    @Test
    void toDTO_WithCompleteReview_ShouldMapAllFields() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Customer");
        customer.setProfilePicture("http://example.com/customer.jpg");

        Review review = new Review();
        review.setId(1L);
        review.setCustomer(customer);
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(10L);
        review.setRating(5);
        review.setComment("Excellent service!");
        review.setImageUrl("http://example.com/review.jpg");
        review.setCreatedAt(LocalDateTime.of(2023, 5, 1, 10, 0));

        // Act
        ReviewDTO dto = reviewMapper.toDTO(review);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getCustomerId());
        assertEquals("John Customer", dto.getCustomerName());
        assertEquals("http://example.com/customer.jpg", dto.getCustomerProfilePic());
        assertEquals(ReviewType.BARBER, dto.getTargetType());
        assertEquals(10L, dto.getTargetId());
        assertEquals(5, dto.getRating());
        assertEquals("Excellent service!", dto.getComment());
        assertEquals("http://example.com/review.jpg", dto.getImageUrl());
        assertEquals(LocalDateTime.of(2023, 5, 1, 10, 0), dto.getCreatedAt());
    }

    @Test
    void toDTO_WithReplies_ShouldMapReplies() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Customer");

        Barber barber = new Barber();
        barber.setId(2L);
        barber.setName("Barber");
        barber.setRole(Roles.BARBER);

        ReviewReply reply1 = new ReviewReply();
        reply1.setId(1L);
        reply1.setUser(barber);
        reply1.setComment("Thank you!");
        reply1.setCreatedAt(LocalDateTime.of(2023, 5, 2, 10, 0));

        Admin admin = new Admin();
        admin.setId(3L);
        admin.setName("Admin");
        admin.setRole(Roles.SHOP_ADMIN);

        ReviewReply reply2 = new ReviewReply();
        reply2.setId(2L);
        reply2.setUser(admin);
        reply2.setComment("We appreciate your feedback");
        reply2.setCreatedAt(LocalDateTime.of(2023, 5, 3, 10, 0));

        Review review = new Review();
        review.setId(1L);
        review.setCustomer(customer);
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(10L);
        review.setRating(5);
        review.setReplies(Arrays.asList(reply1, reply2));

        // Act
        ReviewDTO dto = reviewMapper.toDTO(review);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getReplies());
        assertEquals(2, dto.getReplies().size());
        
        ReviewDTO.ReplyDTO replyDto1 = dto.getReplies().get(0);
        assertEquals(1L, replyDto1.getId());
        assertEquals(2L, replyDto1.getUserId());
        assertEquals("Barber", replyDto1.getUserName());
        assertEquals("BARBER", replyDto1.getUserRole());
        assertEquals("Thank you!", replyDto1.getComment());
        assertEquals(LocalDateTime.of(2023, 5, 2, 10, 0), replyDto1.getCreatedAt());

        ReviewDTO.ReplyDTO replyDto2 = dto.getReplies().get(1);
        assertEquals(2L, replyDto2.getId());
        assertEquals(3L, replyDto2.getUserId());
        assertEquals("Admin", replyDto2.getUserName());
        assertEquals("SHOP_ADMIN", replyDto2.getUserRole());
        assertEquals("We appreciate your feedback", replyDto2.getComment());
        assertEquals(LocalDateTime.of(2023, 5, 3, 10, 0), replyDto2.getCreatedAt());
    }

    @Test
    void toDTO_WithNullReplies_ShouldHandleGracefully() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Customer");

        Review review = new Review();
        review.setId(1L);
        review.setCustomer(customer);
        review.setTargetType(ReviewType.SERVICE);
        review.setTargetId(5L);
        review.setRating(4);
        review.setReplies(null);

        // Act
        ReviewDTO dto = reviewMapper.toDTO(review);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getReplies());
    }

    @Test
    void toEntity_WithCompleteRequest_ShouldMapAllFields() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Customer");

        CreateReviewRequest request = new CreateReviewRequest();
        request.setTargetType(ReviewType.BARBER);
        request.setTargetId(10L);
        request.setRating(5);
        request.setComment("Great haircut!");
        request.setImageUrl("http://example.com/image.jpg");

        // Act
        Review review = reviewMapper.toEntity(request, customer);

        // Assert
        assertNotNull(review);
        assertEquals(customer, review.getCustomer());
        assertEquals(ReviewType.BARBER, review.getTargetType());
        assertEquals(10L, review.getTargetId());
        assertEquals(5, review.getRating());
        assertEquals("Great haircut!", review.getComment());
        assertEquals("http://example.com/image.jpg", review.getImageUrl());
    }

    @Test
    void toEntity_WithServiceReview_ShouldMapCorrectly() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Another Customer");

        CreateReviewRequest request = new CreateReviewRequest();
        request.setTargetType(ReviewType.SERVICE);
        request.setTargetId(5L);
        request.setRating(4);
        request.setComment("Nice service");
        request.setImageUrl(null);

        // Act
        Review review = reviewMapper.toEntity(request, customer);

        // Assert
        assertNotNull(review);
        assertEquals(customer, review.getCustomer());
        assertEquals(ReviewType.SERVICE, review.getTargetType());
        assertEquals(5L, review.getTargetId());
        assertEquals(4, review.getRating());
        assertEquals("Nice service", review.getComment());
        assertNull(review.getImageUrl());
    }

    @Test
    void toEntity_WithNullImageUrl_ShouldMapNull() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(3L);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setTargetType(ReviewType.BARBER);
        request.setTargetId(15L);
        request.setRating(3);
        request.setComment("Average");
        request.setImageUrl(null);

        // Act
        Review review = reviewMapper.toEntity(request, customer);

        // Assert
        assertNotNull(review);
        assertNull(review.getImageUrl());
    }
}
