package gerenciador_musica_backend.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class MusicaCreditosTest {

    @Test
    void deveRegistrarOArtistaPrincipalComoUmUnicoCredito() {
        Artista principal = novoArtista(1L, "Artista principal");

        Musica musica = novaMusica(principal);

        assertThat(musica.getArtistaPrincipal()).isSameAs(principal);
        assertThat(musica.getArtistasParticipantes()).isEmpty();
        assertThat(musica.getCreditosArtistas())
                .singleElement()
                .satisfies(credito -> {
                    assertThat(credito.getMusica()).isSameAs(musica);
                    assertThat(credito.getArtista()).isSameAs(principal);
                    assertThat(credito.getPapel())
                            .isEqualTo(PapelArtistaMusica.PRINCIPAL);
                });
    }

    @Test
    void naoDeveAceitarOArtistaPrincipalComoParticipante() {
        Artista principal = novoArtista(1L, "Artista principal");
        Artista mesmaIdentidade = novoArtista(1L, "Mesmo artista");
        Musica musica = novaMusica(principal);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> musica.setArtistasParticipantes(
                        Set.of(mesmaIdentidade)
                ))
                .withMessage(
                        "O artista principal não pode ser participante."
                );
    }

    @Test
    void devePromoverParticipanteSemManterCreditoDuplicado() {
        Artista principal = novoArtista(1L, "Artista principal");
        Artista participante = novoArtista(2L, "Participante");
        Musica musica = novaMusica(principal);
        musica.setArtistasParticipantes(Set.of(participante));

        musica.setArtistaPrincipal(participante);

        assertThat(musica.getArtistaPrincipal()).isSameAs(participante);
        assertThat(musica.getArtistasParticipantes()).isEmpty();
        assertThat(musica.getCreditosArtistas())
                .singleElement()
                .satisfies(credito -> {
                    assertThat(credito.getArtista()).isSameAs(participante);
                    assertThat(credito.getPapel())
                            .isEqualTo(PapelArtistaMusica.PRINCIPAL);
                });
    }

    @Test
    void deveTrocarOsPapeisSemDuplicarOsArtistas() {
        Artista primeiroPrincipal = novoArtista(1L, "Primeiro artista");
        Artista novoPrincipal = novoArtista(2L, "Segundo artista");
        Musica musica = novaMusica(primeiroPrincipal);
        musica.setArtistasParticipantes(Set.of(novoPrincipal));

        MusicaArtista creditoPrimeiro = buscarCredito(
                musica,
                primeiroPrincipal
        );
        MusicaArtista creditoSegundo = buscarCredito(
                musica,
                novoPrincipal
        );

        musica.definirCreditosArtistas(
                novoPrincipal,
                Set.of(primeiroPrincipal)
        );

        assertThat(musica.getArtistaPrincipal()).isSameAs(novoPrincipal);
        assertThat(musica.getArtistasParticipantes())
                .containsExactly(primeiroPrincipal);
        assertThat(musica.getCreditosArtistas())
                .containsExactlyInAnyOrder(
                        creditoPrimeiro,
                        creditoSegundo
                );
        assertThat(creditoPrimeiro.getPapel())
                .isEqualTo(PapelArtistaMusica.FEAT);
        assertThat(creditoSegundo.getPapel())
                .isEqualTo(PapelArtistaMusica.PRINCIPAL);
    }

    @Test
    void deveRemoverParticipantesAusentesEPreservarOPrincipal() {
        Artista principal = novoArtista(1L, "Artista principal");
        Artista participante = novoArtista(2L, "Participante");
        Musica musica = novaMusica(principal);
        musica.setArtistasParticipantes(Set.of(participante));

        musica.setArtistasParticipantes(null);

        assertThat(musica.getArtistasParticipantes()).isEmpty();
        assertThat(musica.getCreditosArtistas())
                .singleElement()
                .satisfies(credito -> assertThat(credito.getPapel())
                        .isEqualTo(PapelArtistaMusica.PRINCIPAL));
    }

    @Test
    void deveExigirArtistaPrincipal() {
        Artista principal = novoArtista(1L, "Artista principal");
        Musica musica = novaMusica(principal);

        assertThatNullPointerException()
                .isThrownBy(() -> musica.definirCreditosArtistas(
                        null,
                        Set.of()
                ))
                .withMessage("O artista principal é obrigatório.");
    }

    @Test
    void deveRejeitarParticipanteNulo() {
        Artista principal = novoArtista(1L, "Artista principal");
        Musica musica = novaMusica(principal);
        Set<Artista> participantes = new LinkedHashSet<>();
        participantes.add(null);

        assertThatNullPointerException()
                .isThrownBy(() -> musica.setArtistasParticipantes(
                        participantes
                ))
                .withMessage("O artista participante não pode ser nulo.");
    }

    @Test
    void deveCompararIdentificadoresCompostos() {
        MusicaArtistaId primeiro = new MusicaArtistaId(1L, 2L);
        MusicaArtistaId segundo = new MusicaArtistaId();
        segundo.setMusica(1L);
        segundo.setArtista(2L);

        assertThat(segundo.getMusica()).isEqualTo(1L);
        assertThat(segundo.getArtista()).isEqualTo(2L);
        assertThat(primeiro)
                .isEqualTo(segundo)
                .hasSameHashCodeAs(segundo)
                .isNotEqualTo(new MusicaArtistaId(2L, 1L))
                .isNotEqualTo(null)
                .isNotEqualTo("1-2");
    }

    private static Musica novaMusica(Artista principal) {
        return new Musica(
                "Música de teste",
                null,
                180,
                (short) 2026,
                principal,
                null
        );
    }

    private static Artista novoArtista(Long id, String nome) {
        Artista artista = new Artista(
                nome,
                nome + " completo",
                "Descrição de teste.",
                null
        );
        artista.setIdArtista(id);
        return artista;
    }

    private static MusicaArtista buscarCredito(
            Musica musica,
            Artista artista
    ) {
        return musica.getCreditosArtistas()
                .stream()
                .filter(credito -> credito.getArtista() == artista)
                .findFirst()
                .orElseThrow();
    }
}
