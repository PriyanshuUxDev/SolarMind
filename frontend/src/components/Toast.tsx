export default function Toast({ message }: { message: string }) {
  return (
    <div role="status" className="toast">
      {message}
    </div>
  );
}
