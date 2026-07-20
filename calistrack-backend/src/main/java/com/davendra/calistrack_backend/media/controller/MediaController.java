package com.davendra.calistrack_backend.media.controller;

import com.davendra.calistrack_backend.media.dto.CompleteUploadRequest;
import com.davendra.calistrack_backend.media.dto.MediaResponse;
import com.davendra.calistrack_backend.media.dto.UploadRequestDto;
import com.davendra.calistrack_backend.media.dto.UploadRequestResponse;
import com.davendra.calistrack_backend.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Media APIs never accept {@code multipart/form-data}. Clients upload directly to the storage provider.
 */
@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Direct-to-storage media uploads and metadata")
public class MediaController {

	private final MediaService mediaService;

	public MediaController(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	@PostMapping("/upload-request")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Authorize a direct upload", description = "Returns signed upload instructions. No file bytes are sent to this API.")
	public UploadRequestResponse uploadRequest(@Valid @RequestBody UploadRequestDto request) {
		return mediaService.requestUpload(request);
	}

	@PostMapping("/complete")
	@Operation(summary = "Complete and verify an upload", description = "Verifies the object exists at the provider and persists metadata.")
	public MediaResponse complete(@Valid @RequestBody CompleteUploadRequest request) {
		return mediaService.completeUpload(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get media metadata")
	public MediaResponse get(@PathVariable UUID id) {
		return mediaService.getMedia(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Soft-delete media", description = "Deletes the remote object and marks the row deleted.")
	public void delete(@PathVariable UUID id) {
		mediaService.deleteMedia(id);
	}
}
