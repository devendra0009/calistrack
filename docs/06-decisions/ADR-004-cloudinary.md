# ADR-004: Cloudinary for media

## Context

Assessments require video upload. Serving large files from the app server is painful.

## Decision

Store assessment videos (and later avatars/thumbnails) on **Cloudinary**.

## Why

- Upload + CDN without building a file service
- Fits MVP and ~1k users
- Backend only stores URLs

## Consequences

- Need Cloudinary account + env secrets
- Prefer direct/signed upload patterns as volume grows
