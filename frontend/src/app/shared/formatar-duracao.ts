export function formatarDuracao(segundosTotais: number): string {
  const minutos = Math.floor(segundosTotais / 60);
  const segundos = segundosTotais % 60;

  return `${minutos}:${segundos.toString().padStart(2, '0')}`;
}
