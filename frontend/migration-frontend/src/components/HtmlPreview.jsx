import React, { useState } from 'react';

const CheckIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

const DownloadIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
    <polyline points="7 10 12 15 17 10"></polyline>
    <line x1="12" y1="15" x2="12" y2="3"></line>
  </svg>
);

const TagIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="16 18 22 12 16 6"></polyline>
    <polyline points="8 6 2 12 8 18"></polyline>
  </svg>
);

const DatabaseIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
    <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
    <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
  </svg>
);

export default function HtmlPreview({ html, onDownload, isDownloading }) {
  const [activeTab, setActiveTab] = useState('preview');

  // Count basic elements for simple stats
  const getElementCount = () => {
    if (!html) return 0;
    const matches = html.match(/<(h[1-6]|p|li|td)[\s>]/g);
    return matches ? matches.length : 0;
  };

  const getDocSize = () => {
    if (!html) return "0 KB";
    return (html.length / 1024).toFixed(1) + " KB";
  };

  return (
    <div className="html-preview">
      <div className="preview-header">
        <div className="preview-title">
          <CheckIcon /> HTML Generated
        </div>
        <div className="preview-controls">
          <div className="tabs">
            <div 
              className={`tab ${activeTab === 'preview' ? 'active' : ''}`}
              onClick={() => setActiveTab('preview')}
            >
              Preview
            </div>
            <div 
              className={`tab ${activeTab === 'source' ? 'active' : ''}`}
              onClick={() => setActiveTab('source')}
            >
              HTML Source
            </div>
          </div>
          
          <button 
            className="btn btn-ghost" 
            onClick={onDownload}
            disabled={isDownloading || !html}
            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            {isDownloading ? (
              <div className="spinner" style={{ width: '12px', height: '12px', borderWidth: '2px' }} />
            ) : (
              <DownloadIcon />
            )}
            Download
          </button>
        </div>
      </div>

      <div className={`preview-body ${activeTab === 'source' ? 'source-mode' : ''}`}>
        {activeTab === 'preview' ? (
          <iframe 
            title="HTML Preview"
            srcDoc={html}
            sandbox="allow-same-origin"
          />
        ) : (
          <pre className="source-code">
            <code>{html}</code>
          </pre>
        )}
      </div>

      <div className="preview-footer">
        <div className="footer-stat">
          <DatabaseIcon /> {getDocSize()}
        </div>
        <div className="footer-stat">
          <TagIcon /> {getElementCount()} elements parsed
        </div>
      </div>
    </div>
  );
}
