package com.ecommerce.service;

import com.ecommerce.model.ProdutoImagem;
import com.ecommerce.exception.ImagemInvalidaException;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

@ApplicationScoped
public class ProdutoImagemValidator {

    private static final int LIMITE_BYTES = 5 * 1024 * 1024;
    private static final int LIMITE_BASE64 = ((LIMITE_BYTES + 2) / 3) * 4;
    private static final int LIMITE_DIMENSAO = 10_000;
    private static final long LIMITE_PIXELS = 25_000_000L;

    public ProdutoImagem validar(String imagemBase64, boolean removerImagem) {
        if (removerImagem && imagemBase64 != null) {
            throw new ImagemInvalidaException("Envie uma imagem ou solicite sua remocao, nunca os dois juntos");
        }
        if (imagemBase64 == null) {
            return null;
        }

        String tipo;
        String prefixo;
        if (imagemBase64.startsWith("data:image/png;base64,")) {
            tipo = "image/png";
            prefixo = "data:image/png;base64,";
        } else if (imagemBase64.startsWith("data:image/jpeg;base64,")) {
            tipo = "image/jpeg";
            prefixo = "data:image/jpeg;base64,";
        } else {
            throw new ImagemInvalidaException("A imagem deve estar no formato PNG ou JPEG");
        }

        if (imagemBase64.length() - prefixo.length() > LIMITE_BASE64) {
            throw new ImagemInvalidaException("A imagem deve ter no maximo 5 MB");
        }

        byte[] conteudo;
        try {
            conteudo = Base64.getDecoder().decode(imagemBase64.substring(prefixo.length()));
        } catch (IllegalArgumentException e) {
            throw new ImagemInvalidaException("Os dados da imagem sao invalidos");
        }
        if (conteudo.length == 0 || conteudo.length > LIMITE_BYTES) {
            throw new ImagemInvalidaException("A imagem deve ter entre 1 byte e 5 MB");
        }

        validarConteudo(conteudo, tipo);
        ProdutoImagem imagem = new ProdutoImagem();
        imagem.tipo = tipo;
        imagem.conteudo = conteudo;
        return imagem;
    }

    private void validarConteudo(byte[] conteudo, String tipo) {
        try (var entrada = new MemoryCacheImageInputStream(new ByteArrayInputStream(conteudo))) {
            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw new ImagemInvalidaException("O arquivo enviado nao e uma imagem PNG ou JPEG valida");
            }
            ImageReader leitor = leitores.next();
            try {
                String formatoEsperado = tipo.equals("image/png") ? "PNG" : "JPEG";
                if (!formatoEsperado.equalsIgnoreCase(leitor.getFormatName())) {
                    throw new ImagemInvalidaException("O conteudo da imagem nao corresponde ao formato informado");
                }
                leitor.setInput(entrada, true, true);
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura < 1 || altura < 1 || largura > LIMITE_DIMENSAO || altura > LIMITE_DIMENSAO
                        || (long) largura * altura > LIMITE_PIXELS) {
                    throw new ImagemInvalidaException("A imagem deve ter ate 25 megapixels e 10000 pixels por lado");
                }
                BufferedImage imagem = leitor.read(0);
                if (imagem == null) {
                    throw new ImagemInvalidaException("Nao foi possivel ler a imagem enviada");
                }
                imagem.flush();
            } finally {
                leitor.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new ImagemInvalidaException("Nao foi possivel ler a imagem enviada");
        }
    }
}
