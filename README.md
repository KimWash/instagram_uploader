# 인스타그램 vuepress 글 자동 업로드 봇
- thanks to <a href="https://github.com/Bruce0203/bs_meal_1nfo">@Bruce0203/bs_meal_1nfo</a>

## 동작방식
- 블로그 배포 시점에 github action 상에서 jar가 실행된다.
- 배포된 시점에 pwd에는 git repository가 존재하므로 JGit을 이용해 새로운 마크다운 파일을 찾는다.
- 새로운 마크다운 파일이 있으면 내용을 자르고 trim하면서 이미지가 있으면 첫번째 이미지를, 없으면 이미지를 만든다.
- 인스타그램 API를 이용해 업로드한다.
