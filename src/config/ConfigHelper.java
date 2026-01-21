package config;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConfigHelper {
    private final DocumentBuilder documentBuilder;

    public ConfigHelper() {
        DocumentBuilder documentBuilder;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();

        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println("Failed to create xml file parser");
            documentBuilder = null;
        }
        this.documentBuilder = documentBuilder;
    }

    public Config parse(File file) {
        try {
            Document document = this.documentBuilder.parse(file);
            NodeList properties = document.getElementsByTagName("properties");
            if (properties.getLength() > 0) {
                Node propertiesNode = properties.item(0);
                NodeList entriesNodeList = propertiesNode.getChildNodes();
                String version = this.getAttr(null, "version", "1.0");
                Map<String, Config.Entry> entries = new HashMap<>();
                for (int i = 0; i < entriesNodeList.getLength(); i++) {
                    Node node = entriesNodeList.item(i);
                    if (node.getNodeName().equals("entry")) {
                        String key = this.getAttr(node, "key", null);
                        if (key != null) {
                            System.out.println(node.getTextContent());
                            entries.put(key, new Config.Entry(key, Float.parseFloat(node.getTextContent())));
                        }
                    }
                }
                return new Config(entries, version);
            } else {
                System.err.println("Config file doesnt contain properties, failed to parse");
            }
            return Config.EMPTY;
        } catch (SAXException | IOException e) {
            System.err.println("Failed to parse config: " + e.getMessage());
            return Config.EMPTY;
        }
    }

    public void createFile(Config config, File file) throws IOException {
        DOMImplementation domImpl = this.documentBuilder.getDOMImplementation();
        DocumentType doctype = domImpl.createDocumentType(
                "properties",
                null,
                "http://java.sun.com/dtd/properties.dtd"
        );

        Document document = domImpl.createDocument(null, "properties", doctype);
        document.setXmlStandalone(true);
        var root = document.getDocumentElement();
        Date now = new Date();
        var comment = document.createElement("comment");
        comment.setTextContent(now.toString());
        root.appendChild(comment);

        for (Config.Entry entry : config.entries().values()) {
            var entryElem = document.createElement("entry");
            entryElem.setAttribute("key", entry.key());
            entryElem.setTextContent(String.valueOf(entry.value()));
            root.appendChild(entryElem);
        }
        String s = convertDocumentToString(document);
        Files.writeString(Path.of(file.getAbsolutePath()), s);
    }

    private static String convertDocumentToString(Document doc) {
        try {
            // Setup Transformer to pretty-print the XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Output properties for pretty formatting
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Explicitly include the DOCTYPE declaration
            DocumentType doctype = doc.getDoctype();
            if (doctype != null) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
                if (doctype.getPublicId() != null) {
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                }
            }

            // Perform transformation
            StringWriter writer = new StringWriter();
            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(writer);
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error converting Document to String", e);
        }
    }

    protected String getAttr(Node node, String name, String defaultValue) {
        if (node == null) return defaultValue;

        Node attrNode = node.getAttributes().getNamedItem(name);
        if (attrNode instanceof Attr attr) {
            return attr.getValue();
        }
        return defaultValue;
    }

}
