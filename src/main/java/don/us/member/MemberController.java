package don.us.member;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import don.us.funding.FundingController;
import don.us.funding.FundingEntity;
import don.us.funding.FundingMemberEntity;
import don.us.funding.FundingMemberRepository;
import don.us.funding.FundingRepository;
import jakarta.transaction.Transactional;
import util.file.FileController;
import util.file.FileNameVO;

@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/member")
public class MemberController {
	@Autowired
	private MemberRepository repo;

	@Autowired
	private FundingRepository fundingrepo;

	@Autowired
	private FundingMemberRepository fundingmemrepo;

	@Autowired
	private FileController fileController;

	@Value("${realPath.registed_img_path}")
	private String registed_img_path;

	private final String CLIENT_ID = "CdK5qEW_eg3VAa_uRt9l";
	private final String CLIENT_SECRET = "56H_05YtBY";
	private final String REDIRECT_URI = "http://localhost:8090/login/naver";

	@Autowired
	private NaverLogin naverLogin;

	@Autowired
	private FundingController fundingController;

	@PostMapping("devlogin")
	public Map<String, String> myLogin(@RequestBody Map<String, String> map) {
		String input_email = map.get("email");
		Map<String, String> result = new HashMap<>();
		if (repo.findByEmail(input_email).isPresent()/* map.get("name").equals("myID") */) {
			MemberEntity target = repo.findByEmail(input_email).get();
			result.put("result", "success");
			result.put("no", String.valueOf(target.getNo()));
			result.put("email", target.getEmail());
			result.put("name", target.getName());
		} else {
			result.put("result", "fail");
		}
		return result;
	}

	@GetMapping(value = "/login")
	public Map<String, String> login(@RequestParam String code) {
		Map<String, String> result = new HashMap<>();

		String responseBody = GetAccessToken(code);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(responseBody);

			// access_token 값을 추출
			String accessToken = jsonNode.path("access_token").asText();


			// 토큰 값을 사용하여 NaverLogin 클래스의 get_token 메서드 호출 - 여기에 네이버로 로그인한 유저의 정보가 받아와짐
			String user_data = naverLogin.get_token(accessToken);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(user_data);
			JSONObject responseObject = (JSONObject) jsonObject.get("response");


			String user_email = responseObject.get("email").toString();

//			String user_name = responseObject.get("name").toString();
//			String user_birthyear = responseObject.get("birthyear").toString();
//			String user_birthday = responseObject.get("birthday").toString();

			if (repo.findByEmail(user_email).isPresent()) {
				MemberEntity target = repo.findByEmail(user_email).get();

				result.put("result", "success");
				result.put("no", String.valueOf(target.getNo()));
				result.put("email", target.getEmail());
				result.put("name", target.getName());

			} else {
				result.put("result", "fail");
//				result.put("email", user_email);
//				result.put("name", user_name);
//				result.put("birthyear", user_birthyear);
//				result.put("birthday", user_birthday);

			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

//	@GetMapping("/login/success")
//    public String loginSuccess(HttpServletRequest request) {
//        // 세션에서 사용자 정보 가져오기
//        HttpSession session = request.getSession();
//        String userName = (String) session.getAttribute("user_name");
//        String userEmail = (String) session.getAttribute("user_email");
//
//        return "redirect:/localhost:3000/board/list";
//    }

	// 프로필 사진 수정 (신정훈 02 - 16)
	@PostMapping("/changePhoto")
	public String changePhoto(@RequestParam("member_no") int member_no, @RequestParam("file") MultipartFile photo) {
		// 해당 회원 조회
		Optional<MemberEntity> optionalMember = repo.findById(member_no);
		if (optionalMember.isPresent()) {
			MemberEntity memberEntity = optionalMember.get();

			// 파일 업로드 및 변경 로직 구현
			if (photo != null && !photo.isEmpty()) {
				FileNameVO fvo = fileController.upload(photo, registed_img_path);
				memberEntity.setPhoto(fvo.getSaved_filename());

				repo.save(memberEntity);
				return "프로필 사진이 성공적으로 변경되었습니다.";
			} else {
				return "파일이 없습니다.";
			}
		} else {
			return "해당하는 회원이 존재하지 않습니다.";
		}
	}

	// 회원 정보 (신정훈 작업 02 - 14)
	@GetMapping("/info")
	public Optional<MemberEntity> getUserInfo(@RequestParam("member_no") int member_no) {

		Optional<MemberEntity> memberInfo = repo.findById(member_no);

		return memberInfo;
	}

	@GetMapping(value = "/signup")
	public Map<String, String> signup(@RequestParam String code) {
		Map<String, String> result = new HashMap<>();

		String responseBody = GetAccessToken(code);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(responseBody);

			// access_token 값을 추출
			String accessToken = jsonNode.path("access_token").asText();

			// 여기서 accessToken 변수에 추출된 access_token 값이 들어갑니다.

			// 토큰 값을 사용하여 NaverLogin 클래스의 get_token 메서드 호출 - 여기에 네이버로 로그인한 유저의 정보가 받아와짐
			String user_data = naverLogin.get_token(accessToken);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(user_data);
			JSONObject responseObject = (JSONObject) jsonObject.get("response");


			String user_email = responseObject.get("email").toString();

			String user_name = responseObject.get("name").toString();

			MemberEntity mem = new MemberEntity();

			// 같은 이메일로 중복 회원가입하는 것 방지(정보 DB에 넣기 전)
			if (repo.findByEmail(user_email).isPresent()) {
				result.put("result", "fail");
				return result;

			} else {
				mem.setEmail(user_email);
				mem.setName(user_name);
//				mem.setBirthday(full_birthday);

				repo.save(mem);
			}

			// DB에 유저 정보 넣은 후

			// 회원정보를 DB에 넣은 다음에, 그 정보가 잘 들어갔나 체크
			if (repo.findByEmail(user_email).isPresent()) {
				MemberEntity target = repo.findByEmail(user_email).get();
				result.put("result", "success");

			} else {
				result.put("result", "fail");
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// code를 받아와서 tokenUrl로 code를 가지고 재요청. naver에서 accesstoken을 넘겨준다.
	public String GetAccessToken(String code) {
		// Naver OAuth 2.0 Token Endpoint URL
		String tokenUrl = "https://nid.naver.com/oauth2.0/token";

		// Request Headers 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Request Body 설정
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("grant_type", "authorization_code");
		requestBody.add("client_id", CLIENT_ID);
		requestBody.add("client_secret", CLIENT_SECRET);
		requestBody.add("code", code);
		requestBody.add("redirect_uri", REDIRECT_URI);

		// RestTemplate 객체 생성 .
		RestTemplate restTemplate = new RestTemplate();

		// POST 요청 및 응답 받기
		ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, requestBody, String.class);

		// 응답 내용 (JSON 형태의 문자열) - 액세스 토큰이 들어있음
		String responseBody = response.getBody();


		return responseBody;
	}

	// 회원탈퇴
	@Transactional
	@PostMapping("leave")
	public Map<String, String> leave(@RequestBody Map<String, String> map) throws ParseException {
		int member_no = Integer.valueOf(map.get("memberno"));
		List<FundingMemberEntity> nowlist = fundingmemrepo.getNotGaveupFund(member_no);
		Map<String, String> result = new HashMap<>();

		for (int i = 0; i < nowlist.size(); i++) {
			int fundingno = nowlist.get(i).getFundingno();
			Optional<FundingEntity> fund = fundingrepo.findById(fundingno);
			int fund_state = fund.get().getState();
			FundingMemberEntity fund_mem = nowlist.get(i);
			FundingEntity funding = fund.get();

			if (fund_state != 4) {
				// 참여중인 펀드가 있으면 탈퇴가 안됨
				result.put("result", "leave_fail");
				return result;
			}
		}

		// 멤버 테이블에 포인트가 존재한다면 포인트를 다 인출하고 탈퇴해야함
		Optional<MemberEntity> member = repo.findById(member_no);
		if (member.get().getPoint() > 0) {
			result.put("result", "exist_point");
			return result;
		}

		// 삭제 잘 됐는지 체크
		int delete_check = repo.deleteMember(member_no);
		if (delete_check == 1) {
			result.put("result", "leave_finish");
		} else {
			result.put("result", "delete_fail");
		}
		return result;
	}

}