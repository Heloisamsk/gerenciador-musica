export interface PaginaResponse<T> {
  itens: T[];
  paginaAtual: number;
  tamanhoPagina: number;
  totalItens: number;
  totalPaginas: number;
}
