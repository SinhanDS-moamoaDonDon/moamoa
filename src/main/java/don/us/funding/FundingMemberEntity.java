package don.us.funding;

import java.sql.Timestamp;

import org.hibernate.annotations.CurrentTimestamp;

import don.us.member.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "funding_member")
@ToString
public class FundingMemberEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int no;
	
	@Column(name="payment_no")
	private int paymentno;
	
	@Column(name="member_no")
	private int memberno;
	
	@Column(name="member_name")
	private String membername;
	
	@Column(name="funding_no")
	private int fundingno;
	
	@Column(name="funding_type")
	private int fundingtype;
	
	@Column(name="monthly_payment_amount")
	private int monthlypaymentamount;
	
	@Column(name="monthly_payment_date")
	private String monthlypaymentdate;
	
	@Column(name="total_pay_amount")
	private int totalpayamount;
	
	private boolean giveup;
	
	@Column(name="participation_date")
	private Timestamp participationdate;
	
	private int vote;
	
	@Column(name="fund_title")
	private String fundtitle;
	
	private String photo;
	
	@Column(name="invited_date")
	private Timestamp inviteddate;
	
	@Column(name="start_member_no")
	private int startmemberno;
	
	@Column(name="start_member_name")
	private String startmembername;
	
	@Column(name="settlement_amount")
	private String settlementamount;
	
	@Column(name="will_settlement_amount")
	private int willsettlementamount;
}
