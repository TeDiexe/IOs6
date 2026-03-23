package com.album;

public class XmlExportVisitor implements Visitor {
    private StringBuilder xml = new StringBuilder();
    private int indentLevel = 0;

    public String getXml() {
        return xml.toString();
    }

    private String getIndent() {
        return "  ".repeat(indentLevel);
    }

    @Override
    public void visit(Album album) {
        xml.append(getIndent())
                .append("<album name=\"")
                .append(album.getName())
                .append("\">\n");

        indentLevel++;
        for (AlbumElement element : album.getElements()) {
            element.accept(this);
        }
        indentLevel--;

        xml.append(getIndent()).append("</album>\n");
    }

    @Override
    public void visit(RealPhoto photo) {
        xml.append(getIndent())
                .append("<photo type=\"real\" name=\"")
                .append(photo.getName())
                .append("\" format=\"")
                .append(photo.getFormat())
                .append("\" width=\"")
                .append(photo.getResolutionWidth())
                .append("\" />\n");
    }

    @Override
    public void visit(Photo photo) {
        xml.append(getIndent())
                .append("<photo type=\"basic\" name=\"")
                .append(photo.getName())
                .append("\" format=\"")
                .append(photo.getFormat())
                .append("\" />\n");
    }

    @Override
    public void visit(PhotoProxy proxy) {
        xml.append(getIndent())
                .append("<photo type=\"proxy\" name=\"")
                .append(proxy.getName())
                .append("\" />\n");
    }
}