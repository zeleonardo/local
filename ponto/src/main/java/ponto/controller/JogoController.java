package ponto.controller;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import ponto.controller.util.Caminhos;
import ponto.controller.util.CustomLocalDateEditor;
import ponto.controller.util.CustomUsuarioEditor;
import ponto.model.domain.Abono;
import ponto.model.domain.Autorizacao;
import ponto.model.domain.EAbono;
import ponto.model.domain.Jogo;
import ponto.model.domain.Usuario;
import ponto.model.repository.consulta.ConsultaAbono;
import ponto.model.repository.consulta.ConsultaConfiguracao;
import ponto.model.service.ConfiguracaoService;
import ponto.model.service.JogoService;
import ponto.model.service.UsuarioService;
import ponto.util.Mensagens;
import ponto.util.NegocioException;

@Controller
@RequestMapping("/jogo")
@SessionAttributes(value = "jogo", types = Jogo.class)
public class JogoController {

	@Autowired
	private JogoService jogoService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private ConfiguracaoService configuracaoService;

	@Secured(Autorizacao.ROLE_USER)
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ModelAndView doManter(@ModelAttribute Abono abono,
			HttpServletRequest request) {
		ModelAndView mv = new ModelAndView(Caminhos.ABONOS_VISUALIZAR);
		try {
			policy(abono, request);
			jogoService.manterAbono(abono);
			mv.setViewName("redirect:abonos");
		} catch (NegocioException e) {
			mv.addObject(Mensagens.TIPO_DANGER, e.getMensagens());
			mv.addObject("abono", abono);
			addObjects(mv, null);
		}
		return mv;
	}

	private void policy(Abono abono, HttpServletRequest request)
			throws NegocioException {
		if (!request.isUserInRole(Autorizacao.ROLE_ADMIN)
				&& abono.getUsuario() != null
				&& !usuarioService.getUsuarioCorrenteSpring().getId()
						.equals(abono.getUsuario().getId())) {
			throw new NegocioException(Mensagens.ACESSO_NEGADO);
		}
	}

	@Secured(Autorizacao.ROLE_USER)
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView jogar() {
		ModelAndView mv = new ModelAndView(Caminhos.JOGO);
		ConsultaConfiguracao consulta = new ConsultaConfiguracao();
		consulta.setIdUsuario(usuarioService.getUsuarioCorrenteSpring()
				.getId());
		mv.addObject("configuracoes", configuracaoService.consultar(consulta));
		addObjects(mv, null);
		return mv;
	}

	@Secured(Autorizacao.ROLE_USER)
	@RequestMapping(value = "/consultar", method = RequestMethod.POST)
	public ModelAndView pontos(@ModelAttribute ConsultaAbono consulta) {
		ModelAndView mv = new ModelAndView(Caminhos.ABONOS_VISUALIZAR);
		addObjects(mv, consulta);
		return mv;
	}

	@Secured(Autorizacao.ROLE_USER)
	@RequestMapping(value = "/{id}/editar", method = RequestMethod.GET)
	public ModelAndView abonos(@PathVariable("id") String id) {
		ModelAndView mv = new ModelAndView(Caminhos.ABONOS_VISUALIZAR);
		ConsultaAbono consulta = new ConsultaAbono();
		consulta.setId(Long.valueOf(id));
		//mv.addObject("abono", jogoService.buscar(consulta));
		addObjects(mv, null);
		return mv;
	}

	private void addObjects(ModelAndView mv, ConsultaAbono consulta) {
		if (consulta == null) {
			consulta = new ConsultaAbono();
			consulta.setIdUsuario(usuarioService.getUsuarioCorrenteSpring()
					.getId());
		}
		//mv.addObject("abonos", jogoService.consultar(consulta));
		mv.addObject("tipos", EAbono.values());
		mv.addObject("usuarios", usuarioService.consultarUsuariosParaCombobox());
		mv.addObject("consulta", consulta == null ? new ConsultaAbono()
				: consulta);
	}

	@Secured(Autorizacao.ROLE_USER)
	@RequestMapping(value = "/{id}/deletar", method = RequestMethod.GET)
	public ModelAndView deletar(@PathVariable("id") String id,
			HttpServletRequest request) {
		ModelAndView mv = new ModelAndView(Caminhos.ABONOS_VISUALIZAR);
		Abono abono = null;//jogoService.get(Long.valueOf(id));
		try {
			policy(abono, request);
			jogoService.deletar(abono);
			addObjects(mv, null);
		} catch (NegocioException e) {
			mv.addObject(Mensagens.TIPO_DANGER, e.getMensagens());
			mv.addObject("abono", abono);
			addObjects(mv, null);
		}
		return mv;
	}

	@Secured(Autorizacao.ROLE_ADMIN)
	@RequestMapping(value = "/{id}/aprovar", method = RequestMethod.GET)
	public ModelAndView aprovar(@PathVariable("id") String id) {
		ModelAndView mv = new ModelAndView(Caminhos.ABONOS_VISUALIZAR);
		jogoService.aprovar(Long.valueOf(id));
		addObjects(mv, null);
		addObjects(mv, null);
		return mv;
	}

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(LocalDate.class,
				new CustomLocalDateEditor());
		binder.registerCustomEditor(Usuario.class, new CustomUsuarioEditor());
	}

}
