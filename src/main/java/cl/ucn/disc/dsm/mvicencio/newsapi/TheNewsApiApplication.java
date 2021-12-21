package cl.ucn.disc.dsm.mvicencio.newsapi;

import cl.ucn.disc.dsm.mvicencio.newsapi.model.News;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

@SpringBootApplication
/**
 * The News API Application
 *
 *
 * @author Miguel Vicencio
 *
 * **/
public class TheNewsApiApplication {
	/**
	 * the {@link NewsRepository} used to initilize the database
	 */
	@Autowired
	private NewsRepository newsRepository;

	/**
	 * The Main starting point
	 * @param args to use.
	 **/

	public static void main(String[] args) {

		SpringApplication.run(TheNewsApiApplication.class, args);
	}

	/**
	 * initialize the data inside the database
	 * @return the data to use
	 */
	@Bean
	protected InitializingBean initializingDatabase(){
		return () -> {
            final News news = new News(
					 "Apoyo a los campamentos de Antofagasta",
					 "Noticias UCN",
                     "Erika Tello Bianchi",
                     "https://www.noticias.ucn.cl/columnistas/apoyo-a-los-campamentos-de-antofagasta/",
					 "https://www.noticias.ucn.cl/wp-content/uploads/2021/12/nt-campamento.jpg",
					 "Aquello ha quedado impreso en los corazones de nuestros voluntarios y voluntarias y académicos y profesionales: dejar la confortabilidad del espacio universitario e ir en pos de los nuevos vecinos que se allegan al hogar de la antofagastinidad",
					 "El pasado domingo 28 de noviembre inauguramos la sede comunitaria del campamento Nuevo Sol Naciente de Antofagasta, en una jornada esplendorosa coronada por el Astro Rey. Parecía que la principal estrella de nuestra galaxia mostraba el sendero a los habitantes del asentamiento hacia una vida más próspera, satisfactoria en sus anhelos, que cubriera los sueños de sus moradores en este espacio de fraternidad, enseñando valores compartidos.",
                      ZonedDateTime.now(ZoneId.of("-4"))
					        );
			//save the news
			this.newsRepository.save(news);
		};
	}
}
