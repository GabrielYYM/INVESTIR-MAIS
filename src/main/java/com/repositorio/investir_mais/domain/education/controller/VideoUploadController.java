package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.LinkedMultiValueMap;
import java.security.Principal;
import java.util.*;

@RestController @RequestMapping("/api/courses/{courseId}/videos")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class VideoUploadController {
    private final CourseService courses;
    private final RestClient client;
    private final String cloud, key, secret;
    @org.springframework.beans.factory.annotation.Autowired
    public VideoUploadController(CourseService courses, RestClient.Builder builder,
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloud, @Value("${CLOUDINARY_API_KEY:}") String key,
            @Value("${CLOUDINARY_API_SECRET:}") String secret) {
        this(courses, uploadClient(builder), cloud, key, secret);
    }
    VideoUploadController(CourseService courses, RestClient client, String cloud, String key, String secret) {
        this.courses=courses;this.client=client;this.cloud=cloud;this.key=key;this.secret=secret;
    }
    private static RestClient uploadClient(RestClient.Builder builder) {
        var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(
                java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build());
        factory.setReadTimeout(java.time.Duration.ofMinutes(3));
        return builder.requestFactory(factory).build();
    }
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LessonVideo uploadCourseVideo(@PathVariable UUID courseId,@RequestPart MultipartFile file,Principal principal) {
        courses.findCourseOwnedBy(courseId,principal.getName());
        if (!cloud.matches("[A-Za-z0-9_-]+") || key.isBlank() || secret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"O upload de vídeos ainda não foi configurado. Use um link do YouTube.");
        if (file.isEmpty() || file.getSize()>100L*1024*1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Envie um vídeo de até 100 MB.");
        String type=file.getContentType();
        if (!Set.of("video/mp4","video/webm","video/quicktime").contains(type==null?"":type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use vídeo MP4, WebM ou MOV.");
        var body=new LinkedMultiValueMap<String,Object>();
        body.add("file",file.getResource());body.add("public_id","investir-mais/"+courseId+"/"+UUID.randomUUID());
        body.add("allowed_formats","mp4,webm,mov");body.add("overwrite","false");
        try {
            var result=client.post().uri("https://api.cloudinary.com/v1_1/"+cloud+"/video/upload")
                    .headers(headers -> headers.setBasicAuth(key,secret)).contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(Map.class);
            if (result==null || !(result.get("secure_url") instanceof String url)) throw new IllegalArgumentException();
            return LessonVideo.parse(url);
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Não foi possível enviar o vídeo ao Cloudinary. Tente novamente.");
        }
    }
}
