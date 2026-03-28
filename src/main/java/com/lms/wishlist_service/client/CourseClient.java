package com.lms.wishlist_service.client;

import com.lms.wishlist_service.dto.CourseDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", url = "http://localhost:8083")
public interface CourseClient {

    @GetMapping("/courses/{id}")
    CourseDetails getCourseById(@PathVariable("id") Long id);
}