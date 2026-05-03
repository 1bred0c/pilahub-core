# PilaHub Core System

<p align="center">
	<img src="image-readme/pilahub-banner.png" alt="PilaHub Core System banner" />
</p>

<p align="center">
	<img src="image-readme/pilahub-logo.jpg" alt="PilaHub logo" width="220" />
</p>

PilaHub is a Pilates-focused health and fitness management ecosystem, combining body metric tracking, AI posture analysis, session booking, progress tracking, e-commerce, and operational management services within a unified platform.

## Introduction

PilaHub is built to consolidate the fragmented needs of Pilates practitioners into an all-in-one ecosystem:

- **Trainees** can track their health profiles, receive AI-powered posture feedback, and manage their workout schedules.
- **Coaches** can monitor trainees' progress, refine training roadmaps, and conduct 1-on-1 live sessions.
- **Admins and Vendors** can operate the platform, manage orders, products, and track revenue.

## Repository Overview

This repository contains the core components of the PilaHub system:

- `pilahub-backend`: The backend API handling business logic, user management, workout sessions, payments, and AI integration.
- `pilahub-ai-system`: The internal AI system supporting roadmap generation, scoring, workout feedback, and analytical functions.
- `pilahub-ai-model`: The AI models used for posture recognition and movement error deduction.

## Key Features

- Health profile and body metric management.
- AI-assisted posture evaluation during workouts with real-time feedback on body parts needing adjustment.
- Coach scheduling, session booking, and live video session tracking.
- Secure processing of payments, orders, e-wallets, and push notifications.
- Comprehensive dashboards, reports, and administrative workflows for admins, coaches, vendors, and trainees.
- Niche marketplace for Pilates-related products, equipment, and services.

## Tech Stack

- **Backend:** Java Spring Boot.
- **Mobile:** React Native.
- **Web:** Next.js.
- **Database:** PostgreSQL.
- **Cloud & Infrastructure:** Google Cloud Platform (GCP), NGINX.
- **Integrations:** Agora (Live Streaming), Gemini File Search & LLM, VNPay / MoMo (Payments), GHN (Logistics), Firebase Cloud Messaging (FCM).

## System Architecture

### Deployment Overview

<p align="center">
	<img src="image-readme/pilahub-system-arch.png" alt="PilaHub system architecture" />
</p>

The diagram above illustrates the interaction flow between the mobile app, web app, Spring Boot backend, internal AI system, and third-party services such as payment gateways, shipping providers, and video streaming services.

### Use Case Diagram

<p align="center">
	<img src="image-readme/pilahub-use-case-diagram.png" alt="PilaHub use case diagram" />
</p>

The use case diagram fully describes the workflows and roles of the trainee, coach, admin, vendor, IoT devices, AI system, Agora, payment gateways, and GHN throughout the platform's operation.

### Data ERD

<p align="center">
	<img src="image-readme/pilahub-erd.png" alt="PilaHub ERD diagram" />
</p>

The Entity-Relationship Diagram (ERD) represents the core entity groups of the system: accounts, health metrics, exercises, coaching sessions, wallets, orders, shipments, notifications, training roadmaps, and related auxiliary tables.

## Repository Structure

```text
README.md
image-readme/
pilahub-ai-model/
pilahub-ai-system/
pilahub-backend/
