package com.migration.service;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DocxParserService {

    private static final Logger logger = Logger.getLogger(DocxParserService.class.getName());

    private enum ListState {
        BULLET, ORDERED, NONE
    }

    public String convertToHtml(MultipartFile file) throws Exception {
        logger.info("Starting to parse docx file: " + file.getOriginalFilename());

        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                .append("<html>\n<head>\n<meta charset=\"UTF-8\">\n")
                .append("<title>Migration Result</title>\n")
                .append("<style>\n")
                .append("body { font-family: Arial, sans-serif; max-width: 900px; margin: 40px auto; line-height: 1.6; }\n")
                .append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n")
                .append("th, td { border: 1px solid #ccc; padding: 8px; }\n")
                .append("img { max-width: 100%; height: auto; display: block; margin: 10px 0; }\n")
                .append("</style>\n</head>\n<body>\n");

            ListState listState = ListState.NONE;

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    listState = processParagraph(paragraph, html, listState);
                } else if (element instanceof XWPFTable) {
                    listState = closeListIfOpen(html, listState);
                    XWPFTable table = (XWPFTable) element;
                    processTable(table, html);
                }
            }

            // Ensure any open list is closed securely
            closeListIfOpen(html, listState);

            html.append("</body>\n</html>");
            logger.info("Finished parsing docx file.");
            return html.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing docx file", e);
            throw e;
        }
    }

    private ListState processParagraph(XWPFParagraph paragraph, StringBuilder html, ListState listState) {
        String paraText = paragraph.getText().trim();
        String numFmt = paragraph.getNumFmt();
        
        if (numFmt != null) {
            // It's a list item
            ListState targetState = ("bullet".equals(numFmt)) ? ListState.BULLET : ListState.ORDERED;
            
            if (listState != targetState) {
                listState = closeListIfOpen(html, listState);
                if (targetState == ListState.BULLET) {
                    html.append("<ul>\n");
                } else {
                    html.append("<ol>\n");
                }
                listState = targetState;
            }
            html.append("<li>");
            processRuns(paragraph, html);
            html.append("</li>\n");
            return listState;
        }

        // Not a list item
        listState = closeListIfOpen(html, listState);

        // Check if paragraph is truly empty
        if (paraText.isEmpty() && paragraph.getRuns().isEmpty()) {
            return listState;
        }

        String style = paragraph.getStyle();
        int headingLevel = 0;
        if (style != null && style.startsWith("Heading")) {
            try {
                headingLevel = Integer.parseInt(style.substring(7).trim());
            } catch (NumberFormatException e) {
                headingLevel = 0;
            }
        }

        if (headingLevel >= 1 && headingLevel <= 6) {
            html.append("<h").append(headingLevel).append(">");
            processRuns(paragraph, html);
            html.append("</h").append(headingLevel).append(">\n");
        } else {
            html.append("<p>");
            processRuns(paragraph, html);
            html.append("</p>\n");
        }

        return listState;
    }

    private void processRuns(XWPFParagraph paragraph, StringBuilder html) {
        for (IRunElement runElement : paragraph.getIRuns()) {
            if (runElement instanceof XWPFRun) {
                XWPFRun run = (XWPFRun) runElement;
                processImages(run, html);

                String text = run.text();
                if (text != null && !text.isEmpty()) {
                    text = escapeHtml(text);
                    boolean isBold = run.isBold();
                    boolean isItalic = run.isItalic();

                    if (isBold) {
                        html.append("<strong>");
                    }
                    if (isItalic) {
                        html.append("<em>");
                    }

                    html.append(text.replace("\n", "<br/>"));

                    if (isItalic) {
                        html.append("</em>");
                    }
                    if (isBold) {
                        html.append("</strong>");
                    }
                }
            } else if (runElement instanceof XWPFHyperlinkRun) {
                // Not standard run structure
                XWPFHyperlinkRun hyperlinkRun = (XWPFHyperlinkRun) runElement;
                String text = escapeHtml(hyperlinkRun.text());
                String url = hyperlinkRun.getHyperlinkId(); // Gets URL mapping for hyperlinks 
                XWPFHyperlink hyperlink = paragraph.getDocument().getHyperlinkByID(url);
                String actualUrl = (hyperlink != null) ? escapeHtml(hyperlink.getURL()) : "#";
                
                html.append("<a href=\"").append(actualUrl).append("\">").append(text).append("</a>");
            }
        }
        
        // Handle CTHyperlink if any are manually structured that way
        try {
            List<CTHyperlink> cTHyperlinks = paragraph.getCTP().getHyperlinkList();
            if (cTHyperlinks != null && !cTHyperlinks.isEmpty()) {
                // POI merges some runs inside CTHyperlink which we cannot easily extract via generic IRunElement 
                // in all cases without complex XML parsing. Assuming standard XWPFRun / XWPFHyperlinkRun handles most.
                // However, as required, we get the relationship:
                for (CTHyperlink ctLink : cTHyperlinks) {
                    String rId = ctLink.getId();
                    if (rId != null) {
                        PackagePart part = paragraph.getDocument().getPackagePart();
                        PackageRelationship rel = part.getRelationship(rId);
                        if (rel != null) {
                            String url = escapeHtml(rel.getTargetURI().toString());
                            // Basic implementation: we already extracted run elements above, but if it was skipped:
                            // We shouldn't duplicate if XWPFHyperlinkRun handled it.
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error extracting hyperlinks directly from CTP", e);
        }
    }

    private void processImages(XWPFRun run, StringBuilder html) {
        List<XWPFPicture> pictures = run.getEmbeddedPictures();
        for (XWPFPicture pic : pictures) {
            XWPFPictureData picData = pic.getPictureData();
            String mimeType = "image/" + picData.suggestFileExtension();
            byte[] bytes = picData.getData();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            html.append("<img src=\"data:")
                .append(escapeHtml(mimeType))
                .append(";base64,")
                .append(base64)
                .append("\" alt=\"image\" style=\"max-width:100%;height:auto;\" />");
        }
    }

    private void processTable(XWPFTable table, StringBuilder html) {
        html.append("<table>\n");
        List<XWPFTableRow> rows = table.getRows();
        
        for (int i = 0; i < rows.size(); i++) {
            boolean isHeader = (i == 0);
            html.append("  <tr>\n");
            XWPFTableRow row = rows.get(i);
            
            for (XWPFTableCell cell : row.getTableCells()) {
                String tag = isHeader ? "th" : "td";
                html.append("    <").append(tag).append(">");
                // Parse nested paragraphs in cell
                for (XWPFParagraph p : cell.getParagraphs()) {
                    ListState ls = processParagraph(p, html, ListState.NONE);
                    closeListIfOpen(html, ls);
                }
                html.append("</").append(tag).append(">\n");
            }
            html.append("  </tr>\n");
        }
        html.append("</table>\n");
    }

    private ListState closeListIfOpen(StringBuilder html, ListState listState) {
        if (listState == ListState.BULLET) {
            html.append("</ul>\n");
        } else if (listState == ListState.ORDERED) {
            html.append("</ol>\n");
        }
        return ListState.NONE;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                default: builder.append(c); break;
            }
        }
        return builder.toString();
    }
}
