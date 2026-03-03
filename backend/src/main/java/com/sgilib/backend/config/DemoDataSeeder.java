package com.sgilib.backend.config;

import com.sgilib.backend.domain.Author;
import com.sgilib.backend.domain.Book;
import com.sgilib.backend.domain.Client;
import com.sgilib.backend.domain.Sale;
import com.sgilib.backend.repository.AuthorRepository;
import com.sgilib.backend.repository.BookRepository;
import com.sgilib.backend.repository.ClientRepository;
import com.sgilib.backend.repository.SaleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.OffsetDateTime;

@Component
@Profile("!prod")
public class DemoDataSeeder implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final SaleRepository saleRepository;

    public DemoDataSeeder(AuthorRepository authorRepository,
                          BookRepository bookRepository,
                          ClientRepository clientRepository,
                          SaleRepository saleRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.clientRepository = clientRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0 || clientRepository.count() > 0) {
            return;
        }

        Author gabrielGarciaMarquez = new Author();
        gabrielGarciaMarquez.setName("Gabriel Garcia Marquez");
        gabrielGarciaMarquez.setNationality("Colombia");

        Author isabelAllende = new Author();
        isabelAllende.setName("Isabel Allende");
        isabelAllende.setNationality("Chile");

        Author jorgeLuisBorges = new Author();
        jorgeLuisBorges.setName("Jorge Luis Borges");
        jorgeLuisBorges.setNationality("Argentina");

        List<Author> authors = authorRepository.saveAll(List.of(
                gabrielGarciaMarquez,
                isabelAllende,
                jorgeLuisBorges
        ));

        Book cienAnosDeSoledad = new Book();
        cienAnosDeSoledad.setTitle("Cien anos de soledad");
        cienAnosDeSoledad.setIsbn("9780307474728");
        cienAnosDeSoledad.setPublicationYear(1967);
        cienAnosDeSoledad.setStock(8);
        cienAnosDeSoledad.setAuthor(authors.get(0));
        cienAnosDeSoledad.setPublisher("Editorial Sudamericana");
        cienAnosDeSoledad.setCategory("Realismo magico");
        cienAnosDeSoledad.setPageCount(471);
        cienAnosDeSoledad.setDescription("Saga familiar de los Buendia en Macondo.");
        cienAnosDeSoledad.setCoverUrl("https://books.google.com/books/content?id=RYYkGQAACAAJ&printsec=frontcover&img=1&zoom=1");

        Book amorEnLosTiempos = new Book();
        amorEnLosTiempos.setTitle("El amor en los tiempos del colera");
        amorEnLosTiempos.setIsbn("9780307389732");
        amorEnLosTiempos.setPublicationYear(1985);
        amorEnLosTiempos.setStock(5);
        amorEnLosTiempos.setAuthor(authors.get(0));
        amorEnLosTiempos.setPublisher("Editorial Oveja Negra");
        amorEnLosTiempos.setCategory("Novela romantica");
        amorEnLosTiempos.setPageCount(368);
        amorEnLosTiempos.setDescription("Historia de amor tardio entre Fermina Daza y Florentino Ariza.");
        amorEnLosTiempos.setCoverUrl("https://books.google.com/books/content?id=eukWAAAAYAAJ&printsec=frontcover&img=1&zoom=1");

        Book casaDeLosEspiritus = new Book();
        casaDeLosEspiritus.setTitle("La casa de los espiritus");
        casaDeLosEspiritus.setIsbn("9780553383805");
        casaDeLosEspiritus.setPublicationYear(1982);
        casaDeLosEspiritus.setStock(6);
        casaDeLosEspiritus.setAuthor(authors.get(1));
        casaDeLosEspiritus.setPublisher("Plaza y Janes");
        casaDeLosEspiritus.setCategory("Realismo magico");
        casaDeLosEspiritus.setPageCount(433);
        casaDeLosEspiritus.setDescription("Cronica de varias generaciones de la familia Trueba.");
        casaDeLosEspiritus.setCoverUrl("https://books.google.com/books/content?id=77YfAQAAIAAJ&printsec=frontcover&img=1&zoom=1");

        Book evaLuna = new Book();
        evaLuna.setTitle("Eva Luna");
        evaLuna.setIsbn("9780553383829");
        evaLuna.setPublicationYear(1987);
        evaLuna.setStock(4);
        evaLuna.setAuthor(authors.get(1));
        evaLuna.setPublisher("Plaza y Janes");
        evaLuna.setCategory("Novela");
        evaLuna.setPageCount(320);
        evaLuna.setDescription("Relato de vida de Eva Luna en un pais latinoamericano ficticio.");
        evaLuna.setCoverUrl("https://books.google.com/books/content?id=2cgRAQAAIAAJ&printsec=frontcover&img=1&zoom=1");

        Book ficciones = new Book();
        ficciones.setTitle("Ficciones");
        ficciones.setIsbn("9780802130303");
        ficciones.setPublicationYear(1944);
        ficciones.setStock(7);
        ficciones.setAuthor(authors.get(2));
        ficciones.setPublisher("Sur");
        ficciones.setCategory("Cuento");
        ficciones.setPageCount(224);
        ficciones.setDescription("Coleccion de relatos breves fundamentales de Borges.");
        ficciones.setCoverUrl("https://books.google.com/books/content?id=Q8r5AAAACAAJ&printsec=frontcover&img=1&zoom=1");

        bookRepository.saveAll(List.of(
                cienAnosDeSoledad,
                amorEnLosTiempos,
                casaDeLosEspiritus,
                evaLuna,
                ficciones
        ));

        Client client1 = new Client();
        client1.setFirstName("Camila");
        client1.setLastName("Rojas");
        client1.setEmail("camila.rojas@example.com");

        Client client2 = new Client();
        client2.setFirstName("Mateo");
        client2.setLastName("Fernandez");
        client2.setEmail("mateo.fernandez@example.com");

        Client client3 = new Client();
        client3.setFirstName("Valentina");
        client3.setLastName("Garcia");
        client3.setEmail("valentina.garcia@example.com");

        clientRepository.saveAll(List.of(client1, client2, client3));

        Sale sale1 = new Sale();
        sale1.setClient(client1);
        sale1.setBook(cienAnosDeSoledad);
        sale1.setQuantity(1);
        sale1.setSoldAt(OffsetDateTime.now().minusDays(2));
        cienAnosDeSoledad.setStock(cienAnosDeSoledad.getStock() - 1);

        Sale sale2 = new Sale();
        sale2.setClient(client2);
        sale2.setBook(ficciones);
        sale2.setQuantity(2);
        sale2.setSoldAt(OffsetDateTime.now().minusDays(1));
        ficciones.setStock(ficciones.getStock() - 2);

        bookRepository.saveAll(List.of(cienAnosDeSoledad, ficciones));
        saleRepository.saveAll(List.of(sale1, sale2));
    }
}
