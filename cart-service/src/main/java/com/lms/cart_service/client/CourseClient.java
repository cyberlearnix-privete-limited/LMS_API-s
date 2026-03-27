package com.lms.cart_service.client;

// ADD THIS LINE (use your actual package name where the Course class is)
import com.lms.cart_service.dto.CartItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "course-service", url = "http://localhost:8083")
public interface CourseClient {
    @GetMapping("/api/courses/{courseId}")
    CartItem getCourseById(@PathVariable("courseId") String courseId);
}