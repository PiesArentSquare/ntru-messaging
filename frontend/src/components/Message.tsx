export default ({mine, body}: {mine: boolean, body: string}) => {

    const directionalStyles = mine ? 'rounded-bl-2xl self-end bg-brand-primary-500 text-brand-background-50' : 'rounded-br-2xl self-start bg-brand-background-100 text-brand-text-900'

    return <>
        <div className={`font-medium w-64 p-4 rounded-t-2xl mx-4 ${directionalStyles}`}>{body}</div>
    </>
}