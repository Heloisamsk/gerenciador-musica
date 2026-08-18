package gerenciador_musica_backend.dto;

import java.util.List;

/**
 * Envelope genérico para respostas paginadas.
 *
 * @param itens         itens da página atual
 * @param paginaAtual   índice da página atual (começando em 0)
 * @param tamanhoPagina quantidade máxima de itens por página
 * @param totalItens    quantidade total de itens encontrados, considerando todas as páginas
 * @param totalPaginas  quantidade total de páginas disponíveis
 * @param <T>           tipo do item listado
 */
public record PaginaResponseDTO<T>(
        List<T> itens,
        int paginaAtual,
        int tamanhoPagina,
        long totalItens,
        int totalPaginas
) {
}
