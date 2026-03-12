# DocMigrate — Word to Document360

A React frontend for the Document360 Migration Tool. This application allows users to upload Microsoft Word documents (`.docx`), parse them into semantic HTML, preview the parsed content natively, and upload them directly to Document360 as draft or published articles using the Spring Boot backend API.

## Tech Stack
| Technology | Description |
|---|---|
| **React 18** | Frontend rendering library. Functional components, Hooks. |
| **Axios** | Handling promised-based asynchronous REST API queries to the backend. |
| **React-Dropzone** | Beautiful, accessible drag-and-drop HTML5 file uploading. |

## Project Structure
```
migration-frontend/
├── public/                 # Static assets (fonts loaded via index.html)
├── src/                    
│   ├── components/         # Reusable UI Blocks
│   │   ├── FileDropzone.jsx # Drag-and-drop interface for .docx
│   │   ├── HtmlPreview.jsx  # Dual-pane HTML & Iframe Previewer
│   │   └── StatusBanner.jsx # Dynamic Success/Error API feedback
│   ├── services/           
│   │   └── migrationApi.js  # Wrapper around Axios to call HTTP endpoints natively
│   ├── App.jsx             # Main orchestrator linking UI states with the API logic
│   ├── App.css             # Main stylesheet driven by CSS logic and variables
│   └── index.js            # React app entry point
├── package.json            # Node configurations + Proxy definitions
└── README.md
```

## Setup Instructions

### Prerequisites
- Node.js installed
- Java Spring Boot Backend must be running on port `8080`.

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Start the Application**
   ```bash
   npm start
   ```
   > The app runs on `http://localhost:3000`. Wait for the "Backend online" indicator to turn green in the top right.

## Features

1. **Live Health Check Indicator**: A green dot pings the `/api/health` URL to ensure the app is securely piped to the local backend.
2. **Preview**: Leverages Apache POI in the backend to turn headings, lists (bullets and numeric), nested tables, styling, embedded base64 images, and links into pristine semantic HTML.
3. **Dual Preview**: Toggle between a safely-sandboxed iframe visualization and a raw `<pre><code>` HTML block.
4. **Document360 API Integration**: Enter a title (or auto-extract it from the filename) and automatically stream the parsed layout straight to Document360 natively. Shows returned JSON mapping.
5. **Download Source**: Locally generate an `.html` file from the source `.docx`.

## API Endpoints

The React app's `package.json` contains a proxy property: `"proxy": "http://localhost:8080"`. This intelligently resolves any CORS issues seamlessly; the browser proxies `/api/..` requests gracefully to Spring Boot.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Validates Spring Boot backend connection is active. |
| `POST` | `/api/parse` | Transforms `.docx` to HTML (multipart upload). |
| `POST` | `/api/upload` | Takes `file` and `title` keys natively pushing the response into Document360. |
| `POST` | `/api/download-html`| Directly resolves parsed HTML buffer into a downloadable Blob. |
