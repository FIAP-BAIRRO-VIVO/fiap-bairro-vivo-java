package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.repository.TrechoRagView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GuidanceOutputValidator {
    private static final Pattern PHONE = Pattern.compile(
            "(?<!\\d)(?:\\+?55\\s*)?(?:\\(?\\d{2}\\)?\\s*)?9?\\d{4}[-.\\s]?\\d{4}(?!\\d)");
    private static final Pattern LABELED_SHORT_NUMBER = Pattern.compile(
            "(?iu)\\b(?:telefone|tel\\.?|fone|ligue|disque|ligacao|ligação)\\b[^\\d\\n]{0,24}(\\d{3,5})\\b");
    private static final Pattern TIME = Pattern.compile("\\b(?:[01]?\\d|2[0-3]):[0-5]\\d\\b");
    private static final Pattern URL = Pattern.compile("https?://[^\\s)<>'\"\\]]+");

    public void validar(String texto, List<TrechoRagView> fontes) {
        String context = fontes.stream()
                .map(fonte -> fonte.getTitulo() + " " + fonte.getUrl() + " " + fonte.getTexto())
                .reduce("", (first, second) -> first + "\n" + second);
        validarNumeros(texto, context, PHONE, false);
        validarNumeros(texto, context, LABELED_SHORT_NUMBER, true);
        validarNumeros(texto, context, TIME, false);
        validarUrls(texto, fontes);
    }

    private void validarNumeros(String texto, String context, Pattern pattern, boolean useFirstGroup) {
        String contextDigits = context.replaceAll("\\D", "");
        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            String value = useFirstGroup ? matcher.group(1) : matcher.group();
            if (!contextDigits.contains(value.replaceAll("\\D", ""))) {
                throw new IllegalStateException("A resposta contem um numero de contato ou horario sem fonte.");
            }
        }
    }

    private void validarUrls(String texto, List<TrechoRagView> fontes) {
        List<String> officialUrls = fontes.stream()
                .map(fonte -> fonte.getUrl().toLowerCase(Locale.ROOT))
                .toList();
        Matcher matcher = URL.matcher(texto);
        while (matcher.find()) {
            String candidate = matcher.group().replaceAll("[.,;:]+$", "").toLowerCase(Locale.ROOT);
            if (!officialUrls.contains(candidate)) {
                throw new IllegalStateException("A resposta contem um endereco web sem fonte aprovada.");
            }
        }
    }
}
