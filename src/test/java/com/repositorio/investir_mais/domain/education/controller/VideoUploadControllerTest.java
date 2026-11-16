package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class VideoUploadControllerTest {
    @Test void uploadsVideoFromBackendAndReturnsOnlyPublicMediaUrls() {
        var courses=mock(CourseService.class);
        var builder=RestClient.builder();
        var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.cloudinary.com/v1_1/demo/video/upload"))
                .andExpect(header("Authorization","Basic dGVzdC1rZXk6dGVzdC1zZWNyZXQ="))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess("{\"secure_url\":\"https://res.cloudinary.com/demo/video/upload/v1/test.mp4\"}",MediaType.APPLICATION_JSON));
        var controller=new VideoUploadController(courses,builder.build(),"demo","test-key","test-secret");
        var courseId=UUID.randomUUID();
        var result=controller.uploadCourseVideo(courseId,new MockMultipartFile("file","video.mp4","video/mp4",new byte[]{1,2,3}),() -> "teacher@example.test");
        assertEquals("https://res.cloudinary.com/demo/video/upload/v1/test.mp4",result.watchUrl());
        assertTrue(result.thumbnailUrl().endsWith(".jpg"));
        verify(courses).findCourseOwnedBy(courseId,"teacher@example.test");server.verify();
    }
}
