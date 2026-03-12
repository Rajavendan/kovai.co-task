import React from 'react';
import { useDropzone } from 'react-dropzone';

const DocumentIcon = ({ className }) => (
  <svg 
    className={className} 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="2" 
    strokeLinecap="round" 
    strokeLinejoin="round"
  >
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
    <polyline points="14 2 14 8 20 8"></polyline>
    <line x1="16" y1="13" x2="8" y2="13"></line>
    <line x1="16" y1="17" x2="8" y2="17"></line>
    <polyline points="10 9 9 9 8 9"></polyline>
  </svg>
);

const UploadIcon = ({ className }) => (
  <svg 
    className={className} 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="2" 
    strokeLinecap="round" 
    strokeLinejoin="round"
  >
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
    <polyline points="17 8 12 3 7 8"></polyline>
    <line x1="12" y1="3" x2="12" y2="15"></line>
  </svg>
);

export default function FileDropzone({ file, onFileAccepted }) {
  const onDrop = (acceptedFiles) => {
    if (acceptedFiles && acceptedFiles.length > 0) {
      onFileAccepted(acceptedFiles[0]);
    }
  };

  const { getRootProps, getInputProps, isDragActive, isDragReject } = useDropzone({
    onDrop,
    accept: {
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx']
    },
    maxSize: 20 * 1024 * 1024, // 20 MB
    multiple: false
  });

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const handleChangeFile = (e) => {
    e.stopPropagation();
    onFileAccepted(null);
  };

  let dropzoneClass = 'dropzone';
  if (isDragActive && !isDragReject) dropzoneClass += ' drag-active';
  if (isDragReject) dropzoneClass += ' drag-reject';
  if (file) dropzoneClass += ' has-file';

  return (
    <div {...getRootProps()} className={dropzoneClass}>
      <input {...getInputProps()} />
      
      {!file ? (
        <>
          <div className={`dz-icon ${isDragActive ? 'active' : ''}`}>
            <UploadIcon />
          </div>
          <div>
            <div className="dz-text">Drag & drop your .docx file here</div>
            <div className="dz-subtext">or browse to upload (max 20MB)</div>
          </div>
        </>
      ) : (
        <div className="file-card">
          <div className="dz-icon active">
            <DocumentIcon />
          </div>
          <div className="file-name">{file.name}</div>
          <div className="file-size">{formatFileSize(file.size)}</div>
          <button 
            type="button" 
            className="change-file-btn"
            onClick={handleChangeFile}
          >
            Change file
          </button>
        </div>
      )}
    </div>
  );
}
