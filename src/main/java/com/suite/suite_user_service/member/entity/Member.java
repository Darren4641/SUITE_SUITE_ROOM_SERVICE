package com.suite.suite_user_service.member.entity;

import com.suite.suite_user_service.member.dto.ResMemberInfoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "member")
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @OneToOne(mappedBy = "memberId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MemberInfo memberInfo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for(String role : role.split(","))
            authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Builder
    public Member(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public ResMemberInfoDto entityToDto(MemberInfo memberInfo) {
        return ResMemberInfoDto.builder()
                .memberId(memberId)
                .email(email)
                .name(memberInfo.getName())
                .nickName(memberInfo.getNickname())
                .phone(memberInfo.getPhone())
                .securityNum(memberInfo.getSecurityNum())
                .preferStudy(memberInfo.getPreferStudy())
                .location(memberInfo.getLocation())
                .studyMethod(memberInfo.getStudyMethod()).build();
    }

    public void addMemberInfo(MemberInfo memberInfo) {
        this.memberInfo = memberInfo;
        memberInfo.setMemberId(this);
    }


}