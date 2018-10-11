package com.experiment.ivr.usecase;

import com.experiment.ivr.core.core.model.Node;
import com.experiment.ivr.core.core.model.Response;

public class Utils {

    // Base bone implementation of block and choice

    private static String getBlockDocument(String prompt) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vxml xmlns=\"http://www.w3.org/2001/vxml\"\n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "      xsi:schemaLocation=\"http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd\"\n" +
                "      version=\"2.0\">\n" +
                "    <form>\n" +
                "        <block>" + prompt + "</block>\n" +
                "    </form>\n" +
                "</vxml>\n";
    }

    private static String getChoiceDocument(String prompt) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vxml xmlns=\"http://www.w3.org/2001/vxml\"\n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "      xsi:schemaLocation=\"http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd\"\n" +
                "      version=\"2.0\">\n" +
                "    <form>\n" +
                "        <field name=\"drink\">\n" +
                "            <prompt>" + prompt + "</prompt>\n" +
                "            <grammar src=\"drink.grxml\" type=\"application/srgs+xml\"/>\n" +
                "        </field>\n" +
                "        <block>\n" +
                "            <submit next=\"http://www.drink.example.com/drink2.asp\"/>\n" +
                "        </block>\n" +
                "    </form>\n" +
                "</vxml>\n";
    }

    public static String getVXMLDocument(Response response) {
        if(response.getType() == Node.Type.PROMPT) {
            return getBlockDocument(response.getPrompt());
        }

        if(response.getType() == Node.Type.CHOICE) {
            return getChoiceDocument(response.getPrompt());
        }

        return  "";
    }
}
