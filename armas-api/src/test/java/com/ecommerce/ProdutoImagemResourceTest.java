package com.ecommerce;

import com.ecommerce.model.Produto;
import com.ecommerce.model.ProdutoImagem;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class ProdutoImagemResourceTest {

    @Test
    void deveCadastrarPreservarSubstituirRemoverEExcluirImagemSemOrfaos() throws IOException {
        long imagensAntes = contarImagens();
        long categoriaId = criarCategoria("Categoria Imagem Ciclo");
        byte[] png = imagem("png");
        byte[] jpeg = imagem("jpeg");
        Map<String, Object> cadastro = dados(categoriaId, "Produto imagem ciclo");
        cadastro.put("imagemBase64", dataUrl("png", png));
        Response criado = given().contentType(ContentType.JSON).body(cadastro)
                .when().post("/produtos")
                .then().statusCode(201).extract().response();
        long produtoId = criado.jsonPath().getLong("id");
        String primeiraUrl = criado.jsonPath().getString("imagemUrl");
        long primeiraImagemId = imagemId(primeiraUrl);

        try {
            assertEquals("/produtos/" + produtoId + "/imagem?v=" + primeiraImagemId, primeiraUrl);
            verificarImagem(primeiraUrl, "image/png", png);
            Response listagem = given().queryParam("nome", "Produto imagem ciclo")
                    .when().get("/produtos")
                    .then().statusCode(200)
                    .body("items", hasSize(1))
                    .body("items[0].imagemUrl", equalTo(primeiraUrl))
                    .extract().response();
            Map<String, Object> item = listagem.jsonPath().getMap("items[0]");
            assertFalse(item.containsKey("imagemBase64"));
            assertFalse(item.containsKey("imagem"));
            assertFalse(item.containsKey("conteudo"));

            Map<String, Object> edicao = dados(categoriaId, "Produto imagem ciclo atualizado");
            given().contentType(ContentType.JSON).body(edicao)
                    .when().put("/produtos/{id}", produtoId)
                    .then().statusCode(200).body("imagemUrl", equalTo(primeiraUrl));
            edicao.put("imagemBase64", null);
            given().contentType(ContentType.JSON).body(edicao)
                    .when().put("/produtos/{id}", produtoId)
                    .then().statusCode(200).body("imagemUrl", equalTo(primeiraUrl));
            verificarImagem(primeiraUrl, "image/png", png);

            edicao.put("imagemBase64", dataUrl("jpeg", jpeg));
            String segundaUrl = given().contentType(ContentType.JSON).body(edicao)
                    .when().put("/produtos/{id}", produtoId)
                    .then().statusCode(200).extract().jsonPath().getString("imagemUrl");
            assertNotEquals(primeiraUrl, segundaUrl);
            verificarImagem(segundaUrl, "image/jpeg", jpeg);
            verificarImagemRemovida(primeiraImagemId);
            assertEquals(imagensAntes + 1, contarImagens());

            edicao.remove("imagemBase64");
            edicao.put("removerImagem", true);
            given().contentType(ContentType.JSON).body(edicao)
                    .when().put("/produtos/{id}", produtoId)
                    .then().statusCode(200).body("imagemUrl", nullValue());
            given().when().get("/produtos/{id}/imagem", produtoId)
                    .then().statusCode(404).contentType(ContentType.JSON)
                    .body("mensagem", equalTo("Imagem do produto nao encontrada"));
            verificarImagemRemovida(imagemId(segundaUrl));
            assertEquals(imagensAntes, contarImagens());

            edicao.remove("removerImagem");
            edicao.put("imagemBase64", dataUrl("png", png));
            String terceiraUrl = given().contentType(ContentType.JSON).body(edicao)
                    .when().put("/produtos/{id}", produtoId)
                    .then().statusCode(200).extract().jsonPath().getString("imagemUrl");
            given().when().delete("/produtos/{id}", produtoId).then().statusCode(204);
            verificarImagemRemovida(imagemId(terceiraUrl));
            assertEquals(imagensAntes, contarImagens());
            given().when().get("/produtos/{id}/imagem", produtoId)
                    .then().statusCode(404).contentType(ContentType.JSON);
        } finally {
            given().when().delete("/produtos/{id}", produtoId);
        }
    }

    @Test
    void deveRejeitarImagensInvalidasSemAlterarProdutoOuImagemAnterior() throws IOException {
        long categoriaId = criarCategoria("Categoria Imagem Validacao");
        byte[] png = imagem("png");
        Map<String, Object> cadastro = dados(categoriaId, "Produto imagem preservada");
        cadastro.put("imagemBase64", dataUrl("png", png));
        Response criado = given().contentType(ContentType.JSON).body(cadastro)
                .when().post("/produtos")
                .then().statusCode(201).extract().response();
        long produtoId = criado.jsonPath().getLong("id");
        String imagemUrl = criado.jsonPath().getString("imagemUrl");
        long imagensAntes = contarImagens();

        try {
            List<String> invalidas = List.of(
                    dataUrl("png", "nao e uma imagem".getBytes(StandardCharsets.UTF_8)),
                    dataUrl("gif", png),
                    dataUrl("jpeg", png),
                    "data:image/png;base64,%%%",
                    "data:image/png;base64,",
                    dataUrl("png", Arrays.copyOf(png, 40)),
                    dataUrl("png", pngComDimensoes(png, 5001, 5000)),
                    dataUrl("png", pngComDimensoes(png, 10001, 1)),
                    dataUrl("png", new byte[5 * 1024 * 1024 + 1]));

            for (String invalida : invalidas) {
                Map<String, Object> edicao = dados(categoriaId, "Alteracao que deve ser rejeitada");
                edicao.put("imagemBase64", invalida);
                rejeitarEdicao(produtoId, edicao);
                verificarProdutoPreservado(produtoId, imagemUrl);
            }

            Map<String, Object> conflito = dados(categoriaId, "Remocao conflitante");
            conflito.put("imagemBase64", dataUrl("png", png));
            conflito.put("removerImagem", true);
            rejeitarEdicao(produtoId, conflito);
            verificarProdutoPreservado(produtoId, imagemUrl);
            verificarImagem(imagemUrl, "image/png", png);
            assertEquals(imagensAntes, contarImagens());

            long produtosAntes = contarProdutos();
            Map<String, Object> criacaoInvalida = dados(categoriaId, "Criacao rejeitada");
            criacaoInvalida.put("imagemBase64", dataUrl("png", new byte[] {1, 2, 3}));
            given().contentType(ContentType.JSON).body(criacaoInvalida)
                    .when().post("/produtos")
                    .then().statusCode(400).contentType(ContentType.JSON)
                    .body("mensagem", not(blankOrNullString()));
            assertEquals(produtosAntes, contarProdutos());
            assertEquals(imagensAntes, contarImagens());
        } finally {
            given().when().delete("/produtos/{id}", produtoId).then().statusCode(204);
        }
    }

    private void rejeitarEdicao(long produtoId, Map<String, Object> dados) {
        given().contentType(ContentType.JSON).body(dados)
                .when().put("/produtos/{id}", produtoId)
                .then().statusCode(400).contentType(ContentType.JSON)
                .body("mensagem", not(blankOrNullString()));
    }

    private void verificarProdutoPreservado(long produtoId, String imagemUrl) {
        given().when().get("/produtos/{id}", produtoId)
                .then().statusCode(200)
                .body("nome", equalTo("Produto imagem preservada"))
                .body("imagemUrl", equalTo(imagemUrl));
    }

    private void verificarImagem(String url, String tipo, byte[] esperado) {
        byte[] recebido = given().when().get(url)
                .then().statusCode(200).contentType(tipo)
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "no-cache")
                .extract().asByteArray();
        assertArrayEquals(esperado, recebido);
    }

    private void verificarImagemRemovida(long id) {
        assertNull(QuarkusTransaction.requiringNew().call(() -> ProdutoImagem.findById(id)));
    }

    private long contarImagens() {
        return QuarkusTransaction.requiringNew().call(() -> ProdutoImagem.count());
    }

    private long contarProdutos() {
        return QuarkusTransaction.requiringNew().call(() -> Produto.count());
    }

    private long imagemId(String url) {
        return Long.parseLong(url.substring(url.lastIndexOf('=') + 1));
    }

    private Map<String, Object> dados(long categoriaId, String nome) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", nome);
        dados.put("descricao", "Imagem no cadastro de produto");
        dados.put("preco", 150.0);
        dados.put("estoque", 2);
        dados.put("categoriaId", categoriaId);
        return dados;
    }

    private long criarCategoria(String nome) {
        return given().contentType(ContentType.JSON).body(Map.of("nome", nome))
                .when().post("/categorias")
                .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private byte[] imagem(String formato) throws IOException {
        BufferedImage imagem = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        imagem.setRGB(0, 0, 0xB71922);
        imagem.setRGB(1, 0, 0xEEEEEE);
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            ImageIO.write(imagem, formato, saida);
            return saida.toByteArray();
        } finally {
            imagem.flush();
        }
    }

    private String dataUrl(String tipo, byte[] conteudo) {
        return "data:image/" + tipo + ";base64," + Base64.getEncoder().encodeToString(conteudo);
    }

    private byte[] pngComDimensoes(byte[] original, int largura, int altura) {
        byte[] png = original.clone();
        ByteBuffer buffer = ByteBuffer.wrap(png);
        buffer.putInt(16, largura);
        buffer.putInt(20, altura);
        CRC32 crc = new CRC32();
        crc.update(png, 12, 17);
        buffer.putInt(29, (int) crc.getValue());
        return png;
    }
}
