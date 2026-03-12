import React, { useState, useEffect } from 'react';
import migrationApi from './services/migrationApi';
import FileDropzone from './components/FileDropzone';
import HtmlPreview from './components/HtmlPreview';
import StatusBanner from './components/StatusBanner';
import './App.css';

const LogoMark = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="3" y="3" width="18" height="18" rx="4" fill="url(#grad)" />
    <path d="M8 12L12 8L16 12M12 8V16" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <defs>
      <linearGradient id="grad" x1="3" y1="3" x2="21" y2="21" gradientUnits="userSpaceOnUse">
        <stop stopColor="#6366f1" />
        <stop offset="1" stopColor="#8b5cf6" />
      </linearGradient>
    </defs>
  </svg>
);

const Illustration = () => (
  <svg viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect x="60" y="40" width="80" height="100" rx="8" stroke="currentColor" strokeWidth="4" />
    <line x1="75" y1="65" x2="125" y2="65" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
    <line x1="75" y1="85" x2="125" y2="85" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
    <line x1="75" y1="105" x2="105" y2="105" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
    <path d="M120 120 L140 140 L160 120" stroke="var(--indigo)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
    <line x1="140" y1="140" x2="140" y2="60" stroke="var(--indigo)" strokeWidth="4" strokeLinecap="round" />
  </svg>
);

const SupportedIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

export default function App() {
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [htmlContent, setHtmlContent] = useState('');
  const [status, setStatus] = useState(null);
  
  const [backendOnline, setBackendOnline] = useState(null);
  const [isParsing, setIsParsing] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  useEffect(() => {
    // Health check on mount
    migrationApi.healthCheck()
      .then(() => setBackendOnline(true))
      .catch(() => setBackendOnline(false));
  }, []);

  const handleFileAccepted = (selectedFile) => {
    setFile(selectedFile);
    if (!selectedFile) {
      setTitle('');
      setHtmlContent('');
      setStatus(null);
      return;
    }

    if (!title) {
      const defaultTitle = selectedFile.name.replace(/\.docx$/i, '');
      setTitle(defaultTitle);
    }
  };

  const isBusy = isParsing || isUploading || isDownloading;

  const handlePreview = async () => {
    if (!file) return;
    
    setIsParsing(true);
    setStatus(null);
    setHtmlContent('');
    
    try {
      const result = await migrationApi.parseDocument(file);
      if (result.success) {
        setHtmlContent(result.htmlContent);
      } else {
        setStatus({ type: 'error', message: 'Parsing failed.', detail: result.message });
      }
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message;
      setStatus({ type: 'error', message: 'Failed to process document.', detail: errMsg });
    } finally {
      setIsParsing(false);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    
    setIsUploading(true);
    setStatus(null);
    
    try {
      const result = await migrationApi.uploadToDocument360(file, title);
      
      if (result.success) {
        setHtmlContent(result.htmlContent);
        setStatus({
          type: 'success',
          message: result.message || 'Article successfully published!',
          articleId: result.articleId,
          rawResponse: result.document360Response
        });
      } else {
        setStatus({ type: 'error', message: 'Upload failed.', detail: result.message });
      }
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message;
      setStatus({ type: 'error', message: 'Failed to upload document.', detail: errMsg });
    } finally {
      setIsUploading(false);
    }
  };

  const handleDownload = async () => {
    if (!file) return;
    
    setIsDownloading(true);
    try {
      await migrationApi.downloadHtml(file);
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message;
      setStatus({ type: 'error', message: 'Download failed.', detail: errMsg });
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="logo-container">
          <LogoMark />
          <h1 className="app-title">DocMigrate</h1>
          <span className="app-subtitle">Word &rarr; Document360</span>
        </div>
        
        <div className="backend-status">
          <div className={`status-dot ${backendOnline === true ? 'online' : backendOnline === false ? 'offline' : ''}`} />
          {backendOnline === null ? 'Checking...' : backendOnline ? 'Backend online' : 'Backend offline'}
        </div>
      </header>

      <main className="main-content">
        {/* LEFT COLUMN: CONTROLS */}
        <div className="panel controls-panel">
          <div className="section">
            <div className="section-label">
              <span className="step-number">1</span>
              Upload Document
            </div>
            <FileDropzone file={file} onFileAccepted={handleFileAccepted} />
          </div>

          <div className="section">
            <div className="section-label">
              <span className="step-number">2</span>
              Article Title
            </div>
            <input 
              className="title-input"
              type="text" 
              placeholder="e.g. Getting Started Guide" 
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            <span className="input-hint">This will be the article title in Document360</span>
          </div>

          <div className="section" style={{ borderBottom: 'none', paddingBottom: 0 }}>
            <div className="section-label">
              <span className="step-number">3</span>
              Actions
            </div>
            
            <button 
              className="btn btn-secondary" 
              onClick={handlePreview}
              disabled={!file || isBusy}
            >
              {isParsing && <div className="spinner" />}
              {isParsing ? 'Parsing...' : 'Preview HTML'}
            </button>
            
            <button 
              className="btn btn-primary" 
              onClick={handleUpload}
              disabled={!file || isBusy}
            >
              {isUploading && <div className="spinner" />}
              {isUploading ? 'Uploading...' : 'Upload to Document360'}
            </button>
            
            <div className="workflow-legend">
              <div className="legend-item">
                <div className="legend-dot" />
                <span>Parse .docx with Apache POI</span>
              </div>
              <div className="legend-item" style={{ marginTop: '14px' }}>
                <div className="legend-dot" />
                <span>Convert structure to HTML</span>
              </div>
              <div className="legend-item" style={{ marginTop: '14px' }}>
                <div className="legend-dot" />
                <span style={{ color: 'var(--indigo-light)' }}>POST to Document360 API</span>
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: OUTPUT / PREVIEW */}
        <div className="output-panel">
          {status && <StatusBanner status={status} />}
          
          {htmlContent ? (
            <HtmlPreview 
              html={htmlContent} 
              onDownload={handleDownload} 
              isDownloading={isDownloading} 
            />
          ) : !status && (
            <div className="empty-state">
              <div className="empty-illustration">
                <Illustration />
              </div>
              <div className="empty-text">
                Upload a <strong>.docx</strong> file and click <strong>Preview HTML</strong> to see the converted output here.
              </div>
              
              <div style={{ background: 'var(--bg-elevated)', padding: '24px', borderRadius: '12px', width: '100%', maxWidth: '400px' }}>
                <div style={{ fontFamily: 'Syne', fontWeight: 600, marginBottom: '16px', color: 'white', textAlign: 'left' }}>
                  SUPPORTED ELEMENTS
                </div>
                <div className="feature-list">
                  <div className="feature-item"><SupportedIcon /> Headings → h1–h6</div>
                  <div className="feature-item"><SupportedIcon /> Lists → ul / ol</div>
                  <div className="feature-item"><SupportedIcon /> Tables → HTML table</div>
                  <div className="feature-item"><SupportedIcon /> Hyperlinks → anchors</div>
                  <div className="feature-item"><SupportedIcon /> Images → base64 img</div>
                  <div className="feature-item"><SupportedIcon /> Bold & italic text</div>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
