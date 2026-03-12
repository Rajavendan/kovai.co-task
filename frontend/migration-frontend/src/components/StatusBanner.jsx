import React from 'react';

const SuccessIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
    <polyline points="22 4 12 14.01 9 11.01"></polyline>
  </svg>
);

const ErrorIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"></circle>
    <line x1="12" y1="8" x2="12" y2="12"></line>
    <line x1="12" y1="16" x2="12.01" y2="16"></line>
  </svg>
);

export default function StatusBanner({ status }) {
  if (!status) return null;

  const isSuccess = status.type === 'success';

  let prettyJson = '';
  if (isSuccess && status.rawResponse) {
    try {
      // Attempt to parse and pretty-print
      const obj = typeof status.rawResponse === 'string' 
        ? JSON.parse(status.rawResponse) 
        : status.rawResponse;
      prettyJson = JSON.stringify(obj, null, 2);
    } catch (e) {
      prettyJson = status.rawResponse;
    }
  }

  return (
    <div className={`status-banner ${isSuccess ? 'success' : 'error'}`}>
      <div className="status-icon">
        {isSuccess ? <SuccessIcon /> : <ErrorIcon />}
      </div>
      
      <div className="status-content">
        <div className="status-message">{status.message}</div>
        
        {!isSuccess && status.detail && (
          <div className="status-detail-text">{status.detail}</div>
        )}

        {isSuccess && status.articleId && (
          <div className="article-id">
            Article ID: <code>{status.articleId}</code>
          </div>
        )}

        {isSuccess && prettyJson && (
          <details className="raw-response">
            <summary>View Raw JSON Response</summary>
            <pre>
              {prettyJson}
            </pre>
          </details>
        )}
      </div>
    </div>
  );
}
