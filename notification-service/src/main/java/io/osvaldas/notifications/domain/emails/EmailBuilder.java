package io.osvaldas.notifications.domain.emails;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;

public final class EmailBuilder {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    private static final String HTML_TEMPLATE = loadTemplate("email/activation-email.html");

    private static final String PLAIN_TEXT_TEMPLATE = loadTemplate("email/activation-email.txt");

    private EmailBuilder() {
    }

    public static EmailContent buildActivationEmail(String fullName, String activationLink) {
        String name = requireNonNullElse(fullName, "");
        String html = render(HTML_TEMPLATE, htmlEscape(name, UTF_8.name()), htmlEscape(activationLink, UTF_8.name()));
        String plainText = render(PLAIN_TEXT_TEMPLATE, name, activationLink);
        return new EmailContent(plainText, html);
    }

    private static String render(String template, String fullName, String activationLink) {
        return PLACEHOLDER.matcher(template).replaceAll(match -> Matcher.quoteReplacement(switch (match.group(1)) {
            case "fullName" -> fullName;
            case "activationLink" -> activationLink;
            default -> throw new IllegalStateException("Unknown email template placeholder: " + match.group(1));
        }));
    }

    private static String loadTemplate(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load email template: " + path, e);
        }
    }

}
