document.addEventListener('DOMContentLoaded', function() {
    const tagRow = document.getElementById('tagRow');
    if (!tagRow) return;

    tagRow.addEventListener('click', function(e) {
        const tag = e.target.closest('.tag');
        if (!tag) return;

        document.querySelectorAll('.tag').forEach(el => el.classList.remove('active'));
        tag.classList.add('active');

        const tagName = tag.dataset.tag;
        const url = tagName
            ? `/items/fragment?tagName=${encodeURIComponent(tagName)}`
            : `/items/fragment`;          // tagName 없으면 전체 조회

        fetch(url)
            .then(res => res.text())
            .then(html => {
                document.getElementById('scoreGrid').innerHTML = html;
            });
    });
});