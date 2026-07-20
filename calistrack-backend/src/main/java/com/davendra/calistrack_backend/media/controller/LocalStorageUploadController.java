package com.davendra.calistrack_backend.media.controller;

import com.davendra.calistrack_backend.media.provider.local.LocalStorageProvider;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local-provider transport only. Accepts raw PUT bodies — never multipart/form-data.
 */
@RestController
@RequestMapping("/api/v1/media/local")
@Hidden
public class LocalStorageUploadController {

	private final LocalStorageProvider localStorageProvider;

	public LocalStorageUploadController(LocalStorageProvider localStorageProvider) {
		this.localStorageProvider = localStorageProvider;
	}

	@PutMapping("/upload/{token}")
	public ResponseEntity<Void> upload(
			@PathVariable String token,
			HttpServletRequest request
	) throws IOException {
		String contentType = request.getContentType();
		long contentLength = request.getContentLengthLong();
		localStorageProvider.acceptRawUpload(token, contentType, contentLength, request.getInputStream());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/files/**")
	public ResponseEntity<Resource> download(HttpServletRequest request) throws IOException {
		String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String prefix = "/api/v1/media/local/files/";
		String relative = fullPath.startsWith(prefix) ? fullPath.substring(prefix.length()) : fullPath;
		Path file = localStorageProvider.resolveFile(relative);
		String probed = Files.probeContentType(file);
		MediaType mediaType = probed != null ? MediaType.parseMediaType(probed) : MediaType.APPLICATION_OCTET_STREAM;
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
				.contentType(mediaType)
				.body(new FileSystemResource(file));
	}
}
