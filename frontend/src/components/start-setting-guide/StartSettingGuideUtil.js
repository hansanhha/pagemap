import styled from "styled-components";

const SectionContainer = styled.div`
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-top: 3rem;
`;

const Message = styled.div`
`;

const EmphasizedMessage = styled(Message)`
    font-weight: bold;
`;

const pagemapURL = "https://pagemap.net";

export {SectionContainer, Message, EmphasizedMessage, pagemapURL};