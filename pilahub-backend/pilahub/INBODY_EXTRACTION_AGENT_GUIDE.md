# InBody Extraction API Guide (for Main Server Agent)

This guide explains how the Main Server should call the AI System endpoint to extract health metrics from an InBody paper scan image.

## Endpoint

- `POST /api/v1/inbody/extract`
- Supports two content types:
  - `multipart/form-data` (recommended for direct file upload)
  - `application/json` (base64 payload)

## 1) Multipart form-data request

- Required field: `image` (JPEG/PNG/WEBP)
- Optional field: `rawScanId` (string from your system, e.g. `ai_sys_001`)

Example curl:

```bash
curl --location 'http://localhost:8000/api/v1/inbody/extract' \
--form 'image=@"C:/path/to/inbody.jpg"' \
--form 'rawScanId="ai_sys_001"'
```

## 2) Base64 JSON request

Request body:

```json
{
  "base64Image": "<BASE64_IMAGE_OR_DATA_URL>",
  "mimeType": "image/jpeg",
  "rawScanId": "ai_sys_001"
}
```

- `base64Image` accepts both plain base64 and data URL format (e.g. `data:image/jpeg;base64,...`).
- If `mimeType` is empty, server defaults to `image/jpeg`.

Example curl:

```bash
curl --location 'http://localhost:8000/api/v1/inbody/extract' \
--header 'Content-Type: application/json' \
--data '{
  "base64Image": "<PASTE_YOUR_BASE64_STRING_HERE>",
  "mimeType": "image/jpeg",
  "rawScanId": "ai_sys_001"
}'
```

## Success response format

```json
{
  "status": "success",
  "data": {
    "heightCm": 173.0,
    "weightKg": 98.9,
    "bmi": 33.0,
    "bodyFatPercentage": 19.7,
    "muscleMassKg": 46.3,
    "waistCm": null,
    "hipCm": null,
    "source": "INBODY_SCAN",
    "metadata": "{\"device\":\"InBody 270\",\"test_date\":\"2020-05-29 08:18\",\"inbody_score\":98,\"visceral_fat_level\":8,\"raw_scan_id\":\"ai_sys_001\"}"
  },
  "message": "Extracted successfully from InBody 270"
}
```

Notes:

- `waistCm` and `hipCm` are always `null` by design (InBody sheet does not provide these fields in this extraction flow).
- `source` is always `INBODY_SCAN`.
- `metadata` is a JSON string; parse it again on Main Server if you need object access.

## Error handling

- Invalid payload (missing image/base64, unsupported mime type) -> HTTP `400`.
- Unexpected AI/processing errors -> HTTP `500`.
- Error body follows global format (`ErrorResponse`) used by this service.

## Internal Gemini call shape (already handled by AI System)

The AI System sends image + prompt to Gemini with JSON-enforced output:

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "...extraction instructions with strict schema..."
        },
        {
          "inline_data": {
            "mime_type": "image/jpeg",
            "data": "<BASE64>"
          }
        }
      ]
    }
  ],
  "generationConfig": {
    "response_mime_type": "application/json"
  }
}
```

Main Server does not need to call Gemini directly for this flow.

