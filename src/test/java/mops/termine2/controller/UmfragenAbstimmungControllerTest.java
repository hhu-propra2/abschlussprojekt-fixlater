package mops.termine2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UmfragenAbstimmungControllerTest {
	
	@Autowired
	transient MockMvc mvc;
	
	/*  DAS FUNKTIONIERT SO NICHT : BITTE AN TERMINE ABSTIMMUNG CONTROLLER ORIENTIEREN
	@Test
	@WithMockKeycloackAuth(name = Konstanten.STUDENTIN, roles = Konstanten.STUDENTIN)
	void testUmfragenAbstmmung() throws Exception {
		mvc.perform(get("/termine2/umfragen-abstimmung")).andExpect(status().isOk());
	}
	 */
}
