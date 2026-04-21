package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.review.CreateReplyRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.mapper.review.ReviewMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BarberService barberService;

    @Mock
    private BarbershopService barbershopService;

    @Mock
    private EmailService emailService;

    @Mock
    private ServiceOfferingService serviceService;

    @Mock
    private UserService userService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_BarberReview_Success() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Barber barber = new Barber();
        barber.setId(1L);
        barber.setEmail("barber@example.com");
        barber.setName("Jane Barber");

        Barbershop shop = new Barbershop();
        shop.setId(1L);
        barber.setBarbershop(shop);

        Review review = new Review();
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(1L);
        review.setRating(5);
        review.setComment("Great service!");
        review.setCustomer(customer);

        ReviewDTO reviewDTO = new ReviewDTO();

        when(reviewRepository.save(review)).thenReturn(review);
        when(barberService.findById(1L)).thenReturn(barber);
        when(reviewRepository.findAverageRating(ReviewType.BARBER, 1L)).thenReturn(5.0);
        when(reviewRepository.countByTargetTypeAndTargetId(ReviewType.BARBER, 1L)).thenReturn(1L);
        when(reviewMapper.toDTO(review)).thenReturn(reviewDTO);

        // Act
        ReviewDTO result = reviewService.createReview(review);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(review);
        verify(emailService, times(1)).sendNewReviewNotification(anyString(), anyString(), anyString(), anyInt(), anyString());
        verify(notificationService, times(1)).sendReviewSubmittedToBarber(anyLong(), anyString(), anyInt());
    }

    @Test
    void createReview_ServiceReview_WithRating_ThrowsException() {
        // Arrange
        Review review = new Review();
        review.setTargetType(ReviewType.SERVICE);
        review.setRating(5); // Service reviews cannot have ratings

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(review);
        });
    }

    @Test
    void createReview_BarberReview_WithoutRating_ThrowsException() {
        // Arrange
        Review review = new Review();
        review.setTargetType(ReviewType.BARBER);
        review.setRating(null); // Barber reviews must have ratings

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(review);
        });
    }

    @Test
    void createReview_ShopReview_Success() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Review review = new Review();
        review.setTargetType(ReviewType.BARBER_SHOP);
        review.setTargetId(1L);
        review.setRating(4);
        review.setComment("Nice shop!");
        review.setCustomer(customer);

        ReviewDTO reviewDTO = new ReviewDTO();

        when(reviewRepository.save(review)).thenReturn(review);
        when(barbershopService.findById(1L)).thenReturn(shop);
        when(reviewRepository.findAverageRating(ReviewType.BARBER_SHOP, 1L)).thenReturn(4.0);
        when(reviewRepository.countByTargetTypeAndTargetId(ReviewType.BARBER_SHOP, 1L)).thenReturn(1L);
        when(reviewMapper.toDTO(review)).thenReturn(reviewDTO);

        // Act
        ReviewDTO result = reviewService.createReview(review);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void getReviews_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Review review1 = new Review();
        Review review2 = new Review();
        Page<Review> page = new PageImpl<>(Arrays.asList(review1, review2));

        when(reviewRepository.findByTargetTypeAndTargetId(ReviewType.BARBER, 1L, pageable)).thenReturn(page);

        // Act
        Page<Review> result = reviewService.getReviews(ReviewType.BARBER, 1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(reviewRepository, times(1)).findByTargetTypeAndTargetId(ReviewType.BARBER, 1L, pageable);
    }

    @Test
    void replyToReview_MainAdmin_Success() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 1L;

        User mainAdmin = new Admin();
        mainAdmin.setId(userId);
        mainAdmin.setRole(Roles.MAIN_ADMIN);

        Review review = new Review();
        review.setId(reviewId);
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(1L);
        review.setReplies(new ArrayList<>());

        CreateReplyRequest request = new CreateReplyRequest();
        request.setComment("Thank you for your feedback!");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userService.findById(userId)).thenReturn(mainAdmin);
        when(reviewRepository.save(review)).thenReturn(review);

        // Act
        Review result = reviewService.replyToReview(reviewId, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getReplies().size());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void replyToReview_ShopAdmin_ToOwnShopReview_Success() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 1L;

        Barbershop shop = new Barbershop();
        shop.setId(1L);

        Admin shopAdmin = new Admin();
        shopAdmin.setId(userId);
        shopAdmin.setRole(Roles.SHOP_ADMIN);
        shopAdmin.setBarbershop(shop);

        Review review = new Review();
        review.setId(reviewId);
        review.setTargetType(ReviewType.BARBER_SHOP);
        review.setTargetId(1L);
        review.setReplies(new ArrayList<>());

        CreateReplyRequest request = new CreateReplyRequest();
        request.setComment("Thank you!");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userService.findById(userId)).thenReturn(shopAdmin);
        when(reviewRepository.save(review)).thenReturn(review);

        // Act
        Review result = reviewService.replyToReview(reviewId, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getReplies().size());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void replyToReview_Barber_ToOwnReview_Success() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 1L;

        Barber barber = new Barber();
        barber.setId(userId);
        barber.setRole(Roles.BARBER);

        Review review = new Review();
        review.setId(reviewId);
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(userId);
        review.setReplies(new ArrayList<>());

        CreateReplyRequest request = new CreateReplyRequest();
        request.setComment("Thank you!");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userService.findById(userId)).thenReturn(barber);
        when(reviewRepository.save(review)).thenReturn(review);

        // Act
        Review result = reviewService.replyToReview(reviewId, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getReplies().size());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void replyToReview_Unauthorized_ThrowsException() {
        // Arrange
        Long reviewId = 1L;
        Long userId = 2L;

        Barber barber = new Barber();
        barber.setId(userId);
        barber.setRole(Roles.BARBER);

        Review review = new Review();
        review.setId(reviewId);
        review.setTargetType(ReviewType.BARBER);
        review.setTargetId(999L); // Different barber
        review.setReplies(new ArrayList<>());

        CreateReplyRequest request = new CreateReplyRequest();
        request.setComment("Thank you!");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(userService.findById(userId)).thenReturn(barber);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.replyToReview(reviewId, userId, request);
        });
    }

    @Test
    void countByBarbershop_Success() {
        // Arrange
        Barbershop shop = new Barbershop();
        shop.setId(1L);

        when(reviewRepository.countByTargetTypeAndTargetId(ReviewType.BARBER_SHOP, 1L)).thenReturn(10L);

        // Act
        Long result = reviewService.countByBarbershop(shop);

        // Assert
        assertEquals(10L, result);
        verify(reviewRepository, times(1)).countByTargetTypeAndTargetId(ReviewType.BARBER_SHOP, 1L);
    }
}
